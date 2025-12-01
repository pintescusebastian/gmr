package com.mycompany.report_generator.repositories;

import com.mycompany.report_generator.models.Observation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ObservationRepository
    extends JpaRepository<Observation, Long> {
    List<Observation> findByPatientId(Long patientId);
    List<Observation> findByPatientIdOrderByObservationDateDesc(Long patientId);
}
