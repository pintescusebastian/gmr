package com.mycompany.report_generator.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "observations")
public class Observation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(columnDefinition = "TEXT")
    private String symptomsDescription;

    @ElementCollection
    @CollectionTable(
            name = "vital_signs",
            joinColumns = @JoinColumn(name = "observation_id")
    )
    @MapKeyColumn(name = "sign_name")
    @Column(name = "sign_value")
    private Map<String, String> vitalSigns;

    private LocalDateTime observationDate = LocalDateTime.now();

    // Constructor custom pentru service/controller, care folosește Map<String, String>
    public Observation(
            Patient patient,
            Doctor doctor,
            String symptomsDescription,
            Map<String, String> vitalSigns
    ) {
        this.patient = patient;
        this.doctor = doctor;
        this.symptomsDescription = symptomsDescription;
        this.vitalSigns = vitalSigns;
        this.observationDate = LocalDateTime.now();
    }
}