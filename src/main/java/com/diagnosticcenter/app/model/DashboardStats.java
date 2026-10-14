package com.diagnosticcenter.app.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/** Aggregate statistics for the dashboard. Populated from SQL aggregate queries. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private long totalPatients;
    private long totalDoctors;
    private long totalCenters;
    private long totalTests;
    private long totalBookings;
    private long pendingBookings;
    private long confirmedBookings;
    private long completedBookings;
    private long cancelledBookings;
    private BigDecimal totalRevenue;        // sum of amount_paid
    private BigDecimal outstandingAmount;   // sum of outstanding balances
}
