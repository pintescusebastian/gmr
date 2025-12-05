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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ObservationService observationService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public ReportController(ObservationService observationService, PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.observationService = observationService;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @GetMapping("/new")
    public ResponseEntity<Map<String, Object>> getNewReportForm(Authentication authentication) {
        String doctorCode = authentication.getName();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Ready to create new report");
        response.put("doctorCode", doctorCode);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getReportsHistory(Authentication authentication) {
        String doctorCode = authentication.getName();

        // Găsește doctorul
        Doctor doctor = doctorRepository.findByCode(doctorCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));

        // List<Observation> observations = observationService.getObservationsByDoctorId(doctor.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("doctorCode", doctorCode);
        response.put("reports", new java.util.ArrayList<>()); // listă goală deocamdată

        return ResponseEntity.ok(response);
    }

    @PostMapping("/observation")
    public ResponseEntity<Observation> recordObservation(
            @Valid @RequestBody ObservationRequestDTO request
    ) {
        try {
            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found with ID: " + request.getPatientId()));

            Doctor doctor = doctorRepository.findById(request.getDoctorId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found with ID: " + request.getDoctorId()));

            Observation newObservation =
                    observationService.recordNewObservation(patient, doctor, request);

            return new ResponseEntity<>(newObservation, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            throw e;
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