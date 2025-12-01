package com.mycompany.report_generator.repositories;

import com.mycompany.report_generator.models.ObservationReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // Import necesar

import java.util.List;
import java.util.Optional;

@Repository
public interface ObservationReportRepository extends JpaRepository<ObservationReport, Long> {

    List<ObservationReport> findByObservationPatientId(Long patientId);

    @Transactional // Marchează metoda ca operație de modificare a bazei de date
    void deleteByObservationId(Long observationId); // Schimbat la 'void' și Long

    Optional<ObservationReport> findFirstByObservationPatientIdOrderByGenerationDateDesc(Long patientId);

    Optional<ObservationReport> findByObservationId(Long observationId);
}