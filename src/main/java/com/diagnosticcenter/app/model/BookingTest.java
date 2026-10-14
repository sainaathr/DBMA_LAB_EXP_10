package com.diagnosticcenter.app.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/** Line item linking a booking to a specific diagnostic test. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingTest {
    private Integer bookingId;
    private Integer testId;
    private BigDecimal agreedPrice;  // price at time of booking

    // ─── Derived / join fields ──────────────────────────────
    private String testName;
    private Integer durationMinutes;
}
