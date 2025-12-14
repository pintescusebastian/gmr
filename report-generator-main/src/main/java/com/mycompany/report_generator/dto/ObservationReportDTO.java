package com.mycompany.report_generator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObservationReportDTO {
    private Long id;
    private String reportContent;
    private String riskLevel;
    private LocalDateTime generationDate;

    // Observation data
    private ObservationDataDTO observation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ObservationDataDTO {
        private Long id;
        private LocalDateTime observationDate;
        private String symptomsDescription;
        private Map<String, String> vitalSigns;

        // Patient data
        private PatientDataDTO patient;

        // Doctor data
        private DoctorDataDTO doctor;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientDataDTO {
        private String firstName;
        private String lastName;
        private String birthDate;
        private String gender;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorDataDTO {
        private String code;
        private String fullName;
    }
}