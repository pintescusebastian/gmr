package com.mycompany.report_generator.controllers;

import com.mycompany.report_generator.dto.ObservationRequestDTO;
import com.mycompany.report_generator.dto.ObservationReportDTO;
import com.mycompany.report_generator.models.Observation;
import com.mycompany.report_generator.models.ObservationReport;
import com.mycompany.report_generator.models.Patient;
import com.mycompany.report_generator.models.Doctor;
import com.mycompany.report_generator.repositories.PatientRepository;
import com.mycompany.report_generator.repositories.DoctorRepository;
import com.mycompany.report_generator.services.ObservationService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
        System.out.println("DEBUG: Doctor code from JWT: " + doctorCode);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Ready to create new report");
        response.put("doctorCode", doctorCode);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getReportsHistory(Authentication authentication) {
        String doctorCode = authentication.getName();
        System.out.println("DEBUG: Getting history for doctor: " + doctorCode);

        Doctor doctor = doctorRepository.findByCode(doctorCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));

        System.out.println("DEBUG: Doctor ID: " + doctor.getId());

        List<Observation> observations = observationService.getObservationsByDoctorId(doctor.getId());
        System.out.println("DEBUG: Found " + observations.size() + " observations");

        List<Map<String, Object>> reports = observations.stream()
                .map(obs -> {
                    Map<String, Object> report = new HashMap<>();

                    report.put("id", obs.getId());
                    report.put("observationId", obs.getId());
                    report.put("patientName", obs.getPatient().getFirstName() + " " + obs.getPatient().getLastName());
                    report.put("patientId", obs.getPatient().getId());
                    report.put("age", calculateAge(obs.getPatient().getBirthDate()));
                    report.put("date", obs.getObservationDate());
                    report.put("riskLevel", determineRiskLevel(obs));

                    Map<String, String> vitalSigns = obs.getVitalSigns();
                    report.put("heartRate", vitalSigns.getOrDefault("heartRate", "N/A"));
                    report.put("systolicBP", vitalSigns.getOrDefault("systolicBloodPressure", "N/A"));
                    report.put("diastolicBP", vitalSigns.getOrDefault("diastolicBloodPressure", "N/A"));

                    String symptoms = obs.getSymptomsDescription();
                    report.put("diagnosis", symptoms.substring(0, Math.min(100, symptoms.length())) + "...");

                    return report;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("doctorCode", doctorCode);
        response.put("reports", reports);

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

            Observation newObservation = observationService.recordNewObservation(patient, doctor, request);

            return new ResponseEntity<>(newObservation, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            System.err.println("Error recording observation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/generate/{observationId}")
    public ResponseEntity<ObservationReportDTO> generateReport(
            @PathVariable Long observationId
    ) {
        try {
            System.out.println("🔍 API: Fetching report for observation ID: " + observationId);

            ObservationReport report = observationService.getGeneratedReport(observationId);

            System.out.println("✅ Report found, converting to DTO...");

            // Convertim la DTO pentru a evita probleme de serializare
            ObservationReportDTO dto = ObservationReportDTO.builder()
                    .id(report.getId())
                    .reportContent(report.getReportContent())
                    .riskLevel(report.getRiskLevel())
                    .generationDate(report.getGenerationDate())
                    .observation(ObservationReportDTO.ObservationDataDTO.builder()
                            .id(report.getObservation().getId())
                            .observationDate(report.getObservation().getObservationDate())
                            .symptomsDescription(report.getObservation().getSymptomsDescription())
                            .vitalSigns(report.getObservation().getVitalSigns())
                            .patient(ObservationReportDTO.PatientDataDTO.builder()
                                    .firstName(report.getObservation().getPatient().getFirstName())
                                    .lastName(report.getObservation().getPatient().getLastName())
                                    .birthDate(report.getObservation().getPatient().getBirthDate().toString())
                                    .gender(report.getObservation().getPatient().getGender())
                                    .build())
                            .doctor(ObservationReportDTO.DoctorDataDTO.builder()
                                    .code(report.getObservation().getDoctor().getCode())
                                    .fullName(report.getObservation().getDoctor().getFirstName() + " " +
                                            report.getObservation().getDoctor().getLastName())
                                    .build())
                            .build())
                    .build();

            System.out.println("✅ DTO created successfully!");
            return ResponseEntity.ok(dto);

        } catch (RuntimeException e) {
            System.err.println("❌ Error generating report: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Observation>> getPatientHistory(
            @PathVariable Long patientId
    ) {
        try {
            List<Observation> observations = observationService.getObservationsByPatientId(patientId);
            return ResponseEntity.ok(observations);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Helper methods
    private int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private String determineRiskLevel(Observation obs) {
        Map<String, String> vitalSigns = obs.getVitalSigns();

        try {
            int heartRate = Integer.parseInt(vitalSigns.getOrDefault("heartRate", "75"));
            int systolic = Integer.parseInt(vitalSigns.getOrDefault("systolicBloodPressure", "120"));

            if (heartRate > 120 || systolic > 160) {
                return "high";
            } else if (heartRate > 100 || systolic > 140) {
                return "moderate";
            } else {
                return "low";
            }
        } catch (NumberFormatException e) {
            return "low";
        }
    }
}