package com.mycompany.report_generator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@Builder
public class PatientCreationDTO {

    @NotBlank(message = "Prenumele este obligatoriu.")
    private String firstName;

    @NotBlank(message = "Numele de familie este obligatoriu.")
    private String lastName;

    @NotBlank(message = "Data nașterii (birthDate) este obligatorie.")
    private String birthDate;

    @NotBlank(message = "CNP-ul este obligatoriu.")
    private String cnp;

    @NotBlank(message = "Genul (gender) este obligatoriu.")
    private String gender; // M/F/Altele

    private boolean smoker;

    @NotBlank(message = "Nivelul colesterolului (cholesterolStatus) este obligatoriu.")
    private String cholesterolStatus;

    @NotBlank(message = "Istoricul medical este obligatoriu.")
    private String medicalHistory;
}