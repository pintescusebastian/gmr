package com.mycompany.report_generator.services;

import com.mycompany.report_generator.dto.PatientCreationDTO;
import com.mycompany.report_generator.models.Patient;
import java.util.List;

public interface PatientService {
    Patient registerPatient(PatientCreationDTO patientDTO);

    Patient getPatientById(Long id);

    List<Patient> searchPatients(String searchTerm);

    List<Patient> getAllPatients();
}
