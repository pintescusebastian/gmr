package com.mycompany.report_generator.services;

import com.mycompany.report_generator.dto.PatientCreationDTO;
import com.mycompany.report_generator.models.Patient;
import com.mycompany.report_generator.repositories.PatientRepository;
import com.mycompany.report_generator.services.PatientService;
import java.time.LocalDate;
import java.time.format.DateTimeParseException; // Import necesar
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    public PatientServiceImpl(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    @Transactional
    public Patient registerPatient(PatientCreationDTO patientDTO) {
        if (patientRepository.findByCnp(patientDTO.getCnp()).isPresent()) {
            throw new IllegalArgumentException(
                "Un pacient cu CNP-ul " + patientDTO.getCnp() + " există deja."
            );
        }

        Patient patient = new Patient();
        patient.setFirstName(patientDTO.getFirstName());
        patient.setLastName(patientDTO.getLastName());

        try {
            patient.setBirthDate(LocalDate.parse(patientDTO.getBirthDate()));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                "Format dată naștere incorect. Se așteaptă formatul YYYY-MM-DD."
            );
        }

        patient.setCnp(patientDTO.getCnp());

        patient.setGender(patientDTO.getGender());
        patient.setSmoker(patientDTO.isSmoker());
        patient.setCholesterolStatus(patientDTO.getCholesterolStatus());
        patient.setMedicalHistory(patientDTO.getMedicalHistory());

        return patientRepository.save(patient);
    }

    @Override
    public Patient getPatientById(Long id) {
        return patientRepository
            .findById(id)
            .orElseThrow(() ->
                new RuntimeException(
                    "Pacientul cu ID-ul " + id + " nu a fost găsit."
                )
            );
    }

    @Override
    public List<Patient> searchPatients(String searchTerm) {
        if (searchTerm != null && searchTerm.matches("^\\d{13}$")) {
            return patientRepository
                .findByCnp(searchTerm)
                .map(List::of)
                .orElseGet(List::of);
        }

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return patientRepository.findAll();
        } else {
            return patientRepository
                .findAll()
                .stream()
                .filter(
                    p ->
                        (p.getFirstName() +
                            " " +
                            p.getLastName()).toLowerCase().contains(
                            searchTerm.toLowerCase()
                        ) ||
                        p.getCnp().contains(searchTerm)
                )
                .toList();
        }
    }

    @Override
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }
}
