package com.diagnosticcenter.app.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/** Diagnostic center branch. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Center {
    private Integer centerId;

    @NotBlank(message = "Center name is required")
    @Size(max = 150, message = "Center name must not exceed 150 characters")
    private String centerName;

    @NotBlank(message = "Location is required")
    @Size(max = 255)
    private String location;

    @NotBlank(message = "Phone is required")
    @Size(max = 20)
    private String phone;

    @Email(message = "Enter a valid email")
    @Size(max = 100)
    private String email;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity = 50;

    @Size(max = 120)
    private String managerName;

    private LocalDateTime createdAt;

    // ─── Derived / join fields ──────────────────────────────
    private Long doctorCount;
    private Long bookingCount;
}
