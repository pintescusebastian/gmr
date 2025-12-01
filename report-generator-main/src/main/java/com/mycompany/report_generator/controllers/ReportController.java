package com.mycompany.report_generator.controllers;

import com.mycompany.report_generator.dto.ObservationRequestDTO;
import com.mycompany.report_generator.models.Observation;
import com.mycompany.report_generator.models.ObservationReport;
import com.mycompany.report_generator.models.Patient;
import com.mycompany.report_generator.models.Doctor;
import com.mycompany.report_generator.repositories.PatientRepository;
import com.mycompany.report_generator.repositories.DoctorRepository;
import com.mycompany.report_generator.services.ObservationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ObservationService observationService;
    private final PatientRepository patientRepository; // Adăugat
    private final DoctorRepository doctorRepository; // Adăugat

    public ReportController(ObservationService observationService, PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.observationService = observationService;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @PostMapping("/observation")
    public ResponseEntity<Observation> recordObservation(
            @Valid @RequestBody ObservationRequestDTO request
    ) {
        try {
            // 1. Caută Pacientul. Dacă nu există, aruncă o eroare 404.
            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found with ID: " + request.getPatientId()));

            // 2. Caută Doctorul. Dacă nu există, aruncă o eroare 404.
            Doctor doctor = doctorRepository.findById(request.getDoctorId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found with ID: " + request.getDoctorId()));

            // 3. Apelarea Service-ului cu obiectele găsite (Patient, Doctor, DTO)
            Observation newObservation =
                    observationService.recordNewObservation(patient, doctor, request);

            return new ResponseEntity<>(newObservation, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            throw e; // Lăsăm Spring să gestioneze 404
        } catch (RuntimeException e) {
            System.err.println("Error recording observation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/generate/{observationId}")
    public ResponseEntity<ObservationReport> generateReport(
            @PathVariable Long observationId
    ) {
        try {
            ObservationReport report = observationService.getGeneratedReport(
                    observationId
            );
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Observation>> getPatientHistory(
            @PathVariable Long patientId
    ) {
        try {
            List<Observation> observations =
                    observationService.getObservationsByPatientId(patientId);
            return ResponseEntity.ok(observations);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}