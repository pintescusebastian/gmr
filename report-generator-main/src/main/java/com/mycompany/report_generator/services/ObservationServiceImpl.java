package com.mycompany.report_generator.services;

import com.mycompany.report_generator.dto.ObservationRequestDTO;
import com.mycompany.report_generator.models.Doctor;
import com.mycompany.report_generator.models.Observation;
import com.mycompany.report_generator.models.ObservationReport;
import com.mycompany.report_generator.models.Patient;
import com.mycompany.report_generator.repositories.ObservationRepository;
import com.mycompany.report_generator.repositories.ObservationReportRepository;
import com.mycompany.report_generator.repositories.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ObservationServiceImpl implements ObservationService {

    private final ObservationRepository observationRepository;
    private final ObservationReportRepository observationReportRepository;
    private final PatientRepository patientRepository;
    private final ReportGenerationService reportGenerationService;

    public ObservationServiceImpl(
            ObservationRepository observationRepository,
            ObservationReportRepository observationReportRepository,
            PatientRepository patientRepository,
            ReportGenerationService reportGenerationService
    ) {
        this.observationRepository = observationRepository;
        this.observationReportRepository = observationReportRepository;
        this.patientRepository = patientRepository;
        this.reportGenerationService = reportGenerationService;
    }

    @Override
    @Transactional
    public Observation recordNewObservation(Patient patient, Doctor doctor, ObservationRequestDTO request) {
        System.out.println("📝 Recording new observation...");


        Observation observation = new Observation();
        observation.setPatient(patient);
        observation.setDoctor(doctor);
        observation.setSymptomsDescription(request.getSymptomsDescription());
        observation.setVitalSigns(request.getVitalSigns());
        observation.setObservationDate(LocalDateTime.now());


        Observation savedObservation = observationRepository.save(observation);
        System.out.println("✅ Observation saved with ID: " + savedObservation.getId());


        try {
            System.out.println("🤖 Generating AI report for observation ID: " + savedObservation.getId());

            // Apelează serviciul de generare raport
            ObservationReport report = reportGenerationService.generateReport(savedObservation);

            // Salvează raportul în DB
            report.setObservation(savedObservation);
            report.setGenerationDate(LocalDateTime.now());
            observationReportRepository.save(report);

            System.out.println("✅ AI Report generated and saved successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error generating report: " + e.getMessage());
            e.printStackTrace();

        }

        return savedObservation;
    }

    @Override
    @Transactional(readOnly = true)
    public ObservationReport getGeneratedReport(Long observationId) {
        System.out.println("📖 Fetching report for observation ID: " + observationId);


        ObservationReport report = observationReportRepository
                .findByObservationId(observationId)
                .orElseThrow(() -> new RuntimeException("Report not found for observation ID: " + observationId));

        System.out.println("✅ Report found in database!");
        return report;
    }

    @Override
    @Transactional
    public void deleteReportByObservationId(Long observationId) {
        System.out.println("🗑️ Deleting report for observation ID: " + observationId);
        observationReportRepository.deleteByObservationId(observationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Observation> getObservationsByPatientId(Long patientId) {
        System.out.println("📋 Fetching observations for patient ID: " + patientId);

        if (!patientRepository.existsById(patientId)) {
            throw new RuntimeException("Pacientul cu ID-ul " + patientId + " nu a fost găsit.");
        }

        return observationRepository.findByPatientIdOrderByObservationDateDesc(patientId);
    }

    @Override
    @Transactional
    public Observation saveObservation(Observation observation) {
        System.out.println("💾 Saving observation...");
        return observationRepository.save(observation);
    }

    @Override
    @Transactional(readOnly = true)
    public Observation getObservationById(Long observationId) {
        System.out.println("🔍 Fetching observation with ID: " + observationId);
        return observationRepository.findById(observationId)
                .orElseThrow(() -> new RuntimeException("Observation not found with ID: " + observationId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Observation> getObservationsByDoctorId(Long doctorId) {
        System.out.println("📋 Fetching observations for doctor ID: " + doctorId);
        return observationRepository.findByDoctorId(doctorId);
    }
}