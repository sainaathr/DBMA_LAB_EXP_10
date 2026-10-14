package com.diagnosticcenter.app.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** A diagnostic test offered by the center. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Test {
    private Integer testId;

    @NotBlank(message = "Test name is required")
    @Size(max = 150)
    private String testName;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must be ≥ 0")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    private LocalDateTime createdAt;
}
