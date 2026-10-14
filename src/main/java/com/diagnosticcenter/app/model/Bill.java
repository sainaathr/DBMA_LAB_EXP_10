package com.diagnosticcenter.app.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Bill/invoice for a booking. One bill per booking. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bill {
    private Integer billId;

    @NotNull
    private Integer bookingId;

    private BigDecimal totalAmount;
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Amount paid cannot be negative")
    private BigDecimal amountPaid = BigDecimal.ZERO;

    private String paymentStatus;  // Unpaid, Partial, Paid
    private String paymentMethod;  // Cash, Card, UPI, Insurance, Other

    private LocalDateTime billedAt;

    // ─── Derived / join fields ──────────────────────────────
    private String patientName;
    private String centerName;
    private LocalDateTime bookingDate;

    // Computed
    public BigDecimal getOutstandingAmount() {
        if (totalAmount == null) return BigDecimal.ZERO;
        BigDecimal discount = discountAmount == null ? BigDecimal.ZERO : discountAmount;
        BigDecimal paid     = amountPaid    == null ? BigDecimal.ZERO : amountPaid;
        return totalAmount.subtract(discount).subtract(paid);
    }
}
