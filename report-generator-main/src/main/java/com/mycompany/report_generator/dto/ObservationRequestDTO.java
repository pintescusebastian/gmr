package com.mycompany.report_generator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObservationRequestDTO {

    @NotNull(message = "ID-ul pacientului este obligatoriu.")
    private Long patientId;

    @NotNull(message = "ID-ul doctorului este obligatoriu.")
    private Long doctorId;

    @NotBlank(message = "Descrierea simptomelor este obligatorie.")
    private String symptomsDescription;

    @NotNull(message = "Semnele vitale sunt obligatorii.")
    private Map<String, String> vitalSigns;
}