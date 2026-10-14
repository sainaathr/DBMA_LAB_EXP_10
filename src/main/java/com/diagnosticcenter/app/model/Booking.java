package com.diagnosticcenter.app.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Patient appointment/booking at a diagnostic center. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    private Integer bookingId;

    @NotNull(message = "Patient is required")
    private Integer patientId;

    private Integer doctorId;   // optional

    @NotNull(message = "Center is required")
    private Integer centerId;

    @NotNull(message = "Booking date is required")
    private LocalDate bookingDate;

    private LocalTime bookingTime;

    private String status;  // Pending, Confirmed, Completed, Cancelled

    private String notes;

    private LocalDateTime createdAt;

    // ─── Derived / join fields ──────────────────────────────
    private String patientName;
    private String doctorName;
    private String centerName;

    // IDs of selected tests (used in form binding)
    @NotEmpty(message = "Select at least one test")
    private List<Integer> testIds;

    // Line items (populated for detail view)
    private List<BookingTest> tests;
}
