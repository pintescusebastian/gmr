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
        Observation observation = new Observation(
                patient,
                doctor,
                request.getSymptomsDescription(),
                request.getVitalSigns()
        );
        observation.setObservationDate(LocalDateTime.now());
        return observationRepository.save(observation);
    }

    @Override
    @Transactional
    public ObservationReport getGeneratedReport(Long observationId) {
        // Obține Observația. Notă: Această metodă ar trebui să fie scoasă din uz,
        // deoarece ReportController apelează direct reportGenerationService.generateReport(observation).
        // Păstrată de dragul interfeței, dar funcționalitatea principală e delegată mai jos.
        Observation observation = getObservationById(observationId);

        return reportGenerationService.generateReport(observation);
    }

    @Override
    @Transactional
    public void deleteReportByObservationId(Long observationId) {
        // Implementarea corectă folosind repository-ul actualizat
        observationReportRepository.deleteByObservationId(observationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Observation> getObservationsByPatientId(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new RuntimeException("Pacientul cu ID-ul " + patientId + " nu a fost găsit.");
        }
        // Asumăm că ObservationRepository.java a fost actualizat cu această metodă
        return observationRepository.findByPatientIdOrderByObservationDateDesc(patientId);
    }

    @Override
    @Transactional
    public Observation saveObservation(Observation observation) {
        return observationRepository.save(observation);
    }


    @Override
    public Observation getObservationById(Long observationId) {
        return observationRepository.findById(observationId)
                .orElseThrow(() -> new RuntimeException("Observation not found with ID: " + observationId));
    }
}