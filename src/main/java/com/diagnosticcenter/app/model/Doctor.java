package com.diagnosticcenter.app.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/** Doctor assigned to a diagnostic center. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {
    private Integer doctorId;

    @NotBlank(message = "Doctor name is required")
    @Size(max = 120)
    private String doctorName;

    @NotBlank(message = "Specialization is required")
    @Size(max = 100)
    private String specialization;

    @NotBlank(message = "Phone is required")
    @Size(max = 20)
    private String phone;

    @Email(message = "Enter a valid email")
    @Size(max = 100)
    private String email;

    private Integer centerId;

    private LocalDateTime createdAt;

    // ─── Derived / join fields ──────────────────────────────
    private String centerName;   // populated by JOIN query
}
