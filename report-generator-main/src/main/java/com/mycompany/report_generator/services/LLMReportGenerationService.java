package com.mycompany.report_generator.services;

import com.mycompany.report_generator.models.Observation;
import com.mycompany.report_generator.models.ObservationReport;
import com.mycompany.report_generator.repositories.ObservationReportRepository;
import com.mycompany.report_generator.services.LLMClient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.gson.Gson;

@Primary
@Service
public class LLMReportGenerationService implements ReportGenerationService {

    private final LLMClient llmClient;
    private final ObservationReportRepository reportRepository;
    private final Gson gson = new Gson();

    // NOU: Structură plată, simplă, care se potrivește cu schema JSON trimisă către Gemini
    private static class LLMResponse {
        String diagnosis;   // Diagnoză potențială (String)
        String riskLevel;   // Nivel de risc (Scăzut, Mediu, Înalt, Critic) (String)
        String analysis;    // Analiza explicativă (devine reportContent) (String)
    }

    public LLMReportGenerationService(
            LLMClient llmClient,
            ObservationReportRepository reportRepository
    ) {
        this.llmClient = llmClient;
        this.reportRepository = reportRepository;
    }

    @Override
    @Transactional
    public ObservationReport generateReport(Observation observation) {

        // UPSERT: Ștergem raportul vechi înainte de a salva unul nou.
        if (observation.getId() != null) {
            reportRepository.deleteByObservationId(observation.getId());
        }

        String inputPrompt = buildPromptFromObservation(observation);

        if (inputPrompt.startsWith("Observation missing")) {
            return ObservationReport.builder()
                    .patientName("Eroare de Date")
                    .doctorName("Sistem")
                    .reportContent(inputPrompt)
                    .potentialDiagnosis("Date Pacient Incomplete")
                    .riskLevel("N/A")
                    .generationDate(LocalDateTime.now())
                    .build();
        }

        // Răspunsul LLM este garantat a fi un String JSON pur (grație Gemini Structured Output)
        String jsonOutput = llmClient.generateReport(inputPrompt);

        // Verificăm dacă LLMClient a raportat o eroare (ex: cheie API lipsă sau eroare de rețea)
        if (jsonOutput.startsWith("Eroare")) {
            return ObservationReport.builder()
                    .patientName("Eroare API")
                    .doctorName("Sistem")
                    .reportContent(jsonOutput)
                    .potentialDiagnosis("Eroare de Conectare")
                    .riskLevel("Critic")
                    .generationDate(LocalDateTime.now())
                    .build();
        }

        LLMResponse llmResponse;

        try {
            // Parsare directă și simplă, fără manipularea String-urilor
            llmResponse = gson.fromJson(jsonOutput, LLMResponse.class);

            // Verificare critică de integritate
            if (llmResponse == null || llmResponse.analysis == null || llmResponse.diagnosis == null || llmResponse.riskLevel == null) {
                throw new IllegalStateException("Gemini a returnat JSON incomplet, deși schema a fost solicitată.");
            }

        } catch (Exception e) {
            System.err.println("Eroare la parsarea răspunsului JSON. Răspuns brut: " + jsonOutput + ". Eroare: " + e.getMessage());
            return ObservationReport.builder()
                    .observation(observation)
                    .patientName("Eroare la Parsare")
                    .doctorName("Sistem")
                    .reportContent("Eroare la parsarea răspunsului. Răspuns brut: " + jsonOutput)
                    .potentialDiagnosis("Eroare la Parsare")
                    .riskLevel("Critic")
                    .generationDate(LocalDateTime.now())
                    .build();
        }


        // Extrage Nume Pacient & Doctor
        String patientFullName = observation.getPatient() != null
                ? observation.getPatient().getFirstName() + " " + observation.getPatient().getLastName()
                : "Pacient Necunoscut";

        String doctorFullName = observation.getDoctor() != null
                ? observation.getDoctor().getFirstName() + " " + observation.getDoctor().getLastName()
                : "Doctor Necunoscut";

        // Creează entitatea ObservationReport folosind datele structurate
        ObservationReport report = ObservationReport.builder()
                .observation(observation)
                .reportContent(llmResponse.analysis) // Analiza explicativă
                .patientName(patientFullName)
                .doctorName(doctorFullName)
                .potentialDiagnosis(llmResponse.diagnosis) // Diagnoza extrasă
                .riskLevel(llmResponse.riskLevel) // Nivelul de risc extras
                .generationDate(LocalDateTime.now())
                .build();

        return reportRepository.save(report);
    }

    // --- Logica de Construire Prompt - Simplificată ---

    private String buildPromptFromObservation(Observation observation) {
        int age = 0;
        String gender = "necunoscut";
        String smokerStatus = "necunoscut";
        String cholesterolStatus = "necunoscut";
        String medicalHistory = "";

        if (
                observation.getPatient() == null ||
                        observation.getPatient().getBirthDate() == null
        ) {
            return "Observation missing patient data. Cannot generate report.";
        }

        age = calculateAge(observation.getPatient().getBirthDate());

        if (observation.getPatient().getGender() != null) {
            gender = observation.getPatient().getGender().equalsIgnoreCase("M")
                    ? "masculin"
                    : "feminin";
        }
        smokerStatus = observation.getPatient().isSmoker() ? "da" : "nu";

        if (observation.getPatient().getCholesterolStatus() != null) {
            cholesterolStatus = observation.getPatient().getCholesterolStatus();
        }
        if (observation.getPatient().getMedicalHistory() != null) {
            medicalHistory = observation.getPatient().getMedicalHistory();
        }

        String tensiune = observation
                .getVitalSigns()
                .getOrDefault("Tensiune Arterială", "N/A");

        StringBuilder sb = new StringBuilder();

        sb.append("Analizează cazul medical și furnizează răspunsul în format JSON, conform schemei specificate. Răspunde exclusiv în limba română. Diagnoza (diagnosis) și analiza (analysis) trebuie să fie concise și bazate pe fapte.\n\n");
        sb.append("Date Pacient:\n");
        sb.append("- Vârstă: ").append(age).append(" ani\n");
        sb.append("- Sex: ").append(gender).append("\n");
        sb.append("- Tensiune Arterială: ").append(tensiune).append(" mmHg\n");
        sb.append("- Colesterol: ").append(cholesterolStatus).append("\n");
        sb.append("- Fumător: ").append(smokerStatus).append("\n");
        sb.append("Simptome Acute: ").append(observation.getSymptomsDescription()).append("\n");
        sb.append("Istoric Medical: ").append(medicalHistory).append("\n");

        return sb.toString();
    }

    private int calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}