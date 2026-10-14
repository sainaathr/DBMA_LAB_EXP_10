package com.diagnosticcenter.app.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Patient record. All personal data is fictional in the seed data. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    private Integer patientId;

    @NotBlank(message = "Patient name is required")
    @Size(max = 120)
    private String patientName;

    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required")
    private String gender;  // Male, Female, Other

    @NotBlank(message = "Phone is required")
    @Size(max = 20)
    private String phone;

    @Email(message = "Enter a valid email")
    @Size(max = 100)
    private String email;

    private String address;

    private LocalDateTime createdAt;

    // Convenience: age computed from dateOfBirth
    public Integer getAge() {
        if (dateOfBirth == null) return null;
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }
}
