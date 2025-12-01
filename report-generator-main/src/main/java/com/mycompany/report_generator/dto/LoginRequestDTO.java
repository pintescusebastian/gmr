package com.mycompany.report_generator.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDTO {

    @NotBlank(message = "Codul doctorului este obligatoriu.")
    private String doctorCode;

    @NotBlank(message = "Parola este obligatorie.")
    private String password;

    // Manual Getters
    public String getDoctorCode() {
        return doctorCode;
    }

    public String getPassword() {
        return password;
    }

    // Manual Setters
    public void setDoctorCode(String doctorCode) {
        this.doctorCode = doctorCode;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
