package com.mycompany.report_generator.services;

import com.mycompany.report_generator.models.Observation;
import com.mycompany.report_generator.models.ObservationReport;
import com.mycompany.report_generator.repositories.ObservationReportRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportGenerationServiceImpl implements ReportGenerationService {

    private final LLMClient llmClient;
    private final ObservationReportRepository reportRepository;

    public ReportGenerationServiceImpl(
        LLMClient llmClient,
        ObservationReportRepository reportRepository
    ) {
        this.llmClient = llmClient;
        this.reportRepository = reportRepository;
    }

    @Override
    @Transactional
    public ObservationReport generateReport(Observation observation) {
        String llmOutput = llmClient.generateReport("Generare raport simplu.");

        // Folosește constructorul implicit și settere (logica alternativă)
        ObservationReport report = new ObservationReport();

        report.setObservation(observation);
        report.setReportContent(llmOutput);

        // Populează câmpurile adăugate manual
        report.setPatientName(
            observation.getPatient() != null
                ? observation.getPatient().getFirstName() +
                  " " +
                  observation.getPatient().getLastName()
                : "Pacient Necunoscut"
        );
        report.setDoctorName(
            observation.getDoctor() != null
                ? observation.getDoctor().getFirstName() +
                  " " +
                  observation.getDoctor().getLastName()
                : "Doctor Necunoscut"
        );

        report.setPotentialDiagnosis(
            "Default Diagnosis (din serviciul simplu)"
        );
        report.setRiskLevel("Low");
        report.setGenerationDate(LocalDateTime.now());

        return reportRepository.save(report);
    }

    private String buildPromptFromObservation(Observation obs) {
        return (
            "Generic report request for observation: " +
            obs.getSymptomsDescription()
        );
    }
}
