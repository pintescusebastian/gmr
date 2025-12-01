package com.mycompany.report_generator.services;

import com.mycompany.report_generator.dto.ObservationRequestDTO;
import com.mycompany.report_generator.models.Doctor;
import com.mycompany.report_generator.models.Observation;
import com.mycompany.report_generator.models.ObservationReport;
import com.mycompany.report_generator.models.Patient;
import java.util.List;

public interface ObservationService {

    // 1. Înregistrează o nouă observație
    Observation recordNewObservation(Patient patient, Doctor doctor, ObservationRequestDTO request);

    // 2. Obține raportul generat (folosit în ReportController)
    ObservationReport getGeneratedReport(Long observationId);

    // 3. Obține istoricul observațiilor unui pacient
    List<Observation> getObservationsByPatientId(Long patientId);

    // 4. Salvează o observație existentă (utilă pentru extensii)
    Observation saveObservation(Observation observation);

    // 5. NOU: Șterge raportul generat anterior (pentru a remedia eroarea de cheie duplicată la reîncercări)
    void deleteReportByObservationId(Long observationId);

    // 6. NOU: Obține o observație după ID (folosit de ReportController înainte de a chema LLM)
    Observation getObservationById(Long observationId);
}