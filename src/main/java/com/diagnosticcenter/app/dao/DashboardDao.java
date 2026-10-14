package com.diagnosticcenter.app.dao;

import com.diagnosticcenter.app.model.DashboardStats;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO for dashboard aggregate queries.
 * All metrics are fetched from MySQL — no hardcoded values.
 */
@Repository
public class DashboardDao {

    private final JdbcTemplate jdbc;

    public DashboardDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Retrieve all summary statistics using aggregate SQL queries. */
    public DashboardStats getStats() {
        DashboardStats s = new DashboardStats();
        s.setTotalPatients(countFrom("patients"));
        s.setTotalDoctors(countFrom("doctors"));
        s.setTotalCenters(countFrom("centers"));
        s.setTotalTests(countFrom("tests"));
        s.setTotalBookings(countFrom("bookings"));

        // Booking counts by status
        s.setPendingBookings(countBookingsByStatus("Pending"));
        s.setConfirmedBookings(countBookingsByStatus("Confirmed"));
        s.setCompletedBookings(countBookingsByStatus("Completed"));
        s.setCancelledBookings(countBookingsByStatus("Cancelled"));

        // Financial totals from bills table
        String revenueSQL = "SELECT COALESCE(SUM(amount_paid), 0) FROM bills";
        BigDecimal revenue = jdbc.queryForObject(revenueSQL, BigDecimal.class);
        s.setTotalRevenue(revenue != null ? revenue : BigDecimal.ZERO);

        String outstandingSQL =
            "SELECT COALESCE(SUM(total_amount - discount_amount - amount_paid), 0) " +
            "FROM bills WHERE payment_status != 'Paid'";
        BigDecimal outstanding = jdbc.queryForObject(outstandingSQL, BigDecimal.class);
        s.setOutstandingAmount(outstanding != null ? outstanding : BigDecimal.ZERO);

        return s;
    }

    /** Booking counts by day for the last 30 days (for chart). */
    public List<Map<String, Object>> getBookingTrend() {
        String sql =
            "SELECT DATE(booking_date) AS day, COUNT(*) AS total " +
            "FROM bookings " +
            "WHERE booking_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "GROUP BY day ORDER BY day";
        return jdbc.queryForList(sql);
    }

    /** Revenue by day for the last 30 days (for chart). */
    public List<Map<String, Object>> getRevenueTrend() {
        String sql =
            "SELECT DATE(b.billed_at) AS day, COALESCE(SUM(b.amount_paid), 0) AS revenue " +
            "FROM bills b " +
            "WHERE b.billed_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
            "GROUP BY day ORDER BY day";
        return jdbc.queryForList(sql);
    }

    /** Booking status distribution (for pie chart). */
    public Map<String, Long> getBookingStatusDistribution() {
        String sql = "SELECT status, COUNT(*) AS cnt FROM bookings GROUP BY status";
        Map<String, Long> result = new HashMap<>();
        jdbc.query(sql, rs -> {
            result.put(rs.getString("status"), rs.getLong("cnt"));
        });
        return result;
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private long countFrom(String tableName) {
        // Table name is internal constant — not user input, so safe to inline
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
    }

    private long countBookingsByStatus(String status) {
        return jdbc.queryForObject(
            "SELECT COUNT(*) FROM bookings WHERE status = ?", Long.class, status);
    }
}
