package com.mycompany.report_generator.controllers;

import com.mycompany.report_generator.dto.PatientCreationDTO;
import com.mycompany.report_generator.models.Patient;
import com.mycompany.report_generator.services.PatientService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    public ResponseEntity<Patient> registerPatient(
        @Valid @RequestBody PatientCreationDTO patientDTO
    ) {
        try {
            Patient newPatient = patientService.registerPatient(patientDTO);
            return new ResponseEntity<>(newPatient, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Patient>> searchPatients(
        @RequestParam(required = false) String search
    ) {
        List<Patient> patients;
        if (search == null || search.trim().isEmpty()) {
            patients = patientService.getAllPatients();
        } else {
            patients = patientService.searchPatients(search);
        }
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        try {
            Patient patient = patientService.getPatientById(id);
            return ResponseEntity.ok(patient);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
