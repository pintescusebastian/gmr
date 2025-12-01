package com.mycompany.report_generator;

import com.mycompany.report_generator.models.Doctor;
import com.mycompany.report_generator.models.Observation;
import com.mycompany.report_generator.models.ObservationReport;
import com.mycompany.report_generator.models.Patient;
import com.mycompany.report_generator.services.ReportGenerationServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReportGeneratorApplicationTests {

    @Autowired
    private ReportGenerationServiceImpl reportService;

    @Test
    void testGenerateReport_basic() {
        Patient patient = new Patient();
        patient.setFirstName("John");
        patient.setLastName("Doe");

        Doctor doctor = new Doctor();
        doctor.setFirstName("Alice");
        doctor.setLastName("Smith");

        Observation observation = new Observation(patient, doctor,
                "Chest pain", Map.of("blood_pressure", "150/90", "heart_rate", "110"));

        ObservationReport report = reportService.generateReport(observation);

        assertEquals("John Doe", report.getPatientName());
        assertEquals("Alice Smith", report.getDoctorName());
        assertEquals("Chest pain", report.getObservationSummary());
        assertTrue(report.getRiskFactors().contains("High Blood Pressure"));
        assertTrue(report.getRiskFactors().contains("High Heart Rate"));
        assertEquals("Cardiovascular Risk", report.getPotentialDiagnosis());
    }

    @Test
    void testGenerateReport_withRiskFactors() {
        Patient patient = new Patient();
        Doctor doctor = new Doctor();
        Map<String, String> vitalSigns = new HashMap<>();
        vitalSigns.put("blood_pressure", "150/100");

        Observation observation = new Observation(
                patient,
                doctor,
                "High blood pressure and chest pain.",
                vitalSigns
        );

        ObservationReport report = reportService.generateReport(observation);

        assertNotNull(report);
        assertTrue(report.getObservationSummary().contains("chest pain"));
        assertNotEquals("Not Implemented", report.getPotentialDiagnosis(),
                "Diagnosis should be computed, not hardcoded.");
    }
}
