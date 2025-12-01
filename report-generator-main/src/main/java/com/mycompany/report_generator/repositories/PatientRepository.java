package com.mycompany.report_generator.repositories;

import com.mycompany.report_generator.models.Patient;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByCnp(String cnp);

    Optional<Patient> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(
        String firstName,
        String lastName
    );
}
