package com.diagnosticcenter.app.dao;

import com.diagnosticcenter.app.model.Bill;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO for bills/invoices using JdbcTemplate (JDBC). */
@Repository
public class BillDao {

    private final JdbcTemplate jdbc;

    public BillDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Bill> billRowMapper = (rs, rowNum) -> {
        Bill b = new Bill();
        b.setBillId(rs.getInt("bill_id"));
        b.setBookingId(rs.getInt("booking_id"));
        b.setTotalAmount(rs.getBigDecimal("total_amount"));
        b.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        b.setAmountPaid(rs.getBigDecimal("amount_paid"));
        b.setPaymentStatus(rs.getString("payment_status"));
        b.setPaymentMethod(rs.getString("payment_method"));
        Timestamp bAt = rs.getTimestamp("billed_at");
        if (bAt != null) b.setBilledAt(bAt.toLocalDateTime());
        try { b.setPatientName(rs.getString("patient_name")); } catch (Exception ignored) {}
        try { b.setCenterName(rs.getString("center_name")); } catch (Exception ignored) {}
        return b;
    };

    /** All bills with patient and center info. */
    public List<Bill> findAll(String status, int offset, int limit) {
        StringBuilder sql = new StringBuilder(
            "SELECT bl.*, p.patient_name, c.center_name " +
            "FROM bills bl " +
            "JOIN bookings bk ON bl.booking_id = bk.booking_id " +
            "JOIN patients p  ON bk.patient_id = p.patient_id " +
            "JOIN centers c   ON bk.center_id = c.center_id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            sql.append(" AND bl.payment_status = ?"); params.add(status);
        }
        sql.append(" ORDER BY bl.billed_at DESC LIMIT ? OFFSET ?");
        params.add(limit); params.add(offset);
        return jdbc.query(sql.toString(), billRowMapper, params.toArray());
    }

    public int countAll(String status) {
        if (status != null && !status.isBlank())
            return jdbc.queryForObject("SELECT COUNT(*) FROM bills WHERE payment_status = ?", Integer.class, status);
        return jdbc.queryForObject("SELECT COUNT(*) FROM bills", Integer.class);
    }

    public Optional<Bill> findById(int billId) {
        String sql = "SELECT bl.*, p.patient_name, c.center_name " +
            "FROM bills bl " +
            "JOIN bookings bk ON bl.booking_id = bk.booking_id " +
            "JOIN patients p  ON bk.patient_id = p.patient_id " +
            "JOIN centers c   ON bk.center_id  = c.center_id WHERE bl.bill_id = ?";
        return jdbc.query(sql, billRowMapper, billId).stream().findFirst();
    }

    public Optional<Bill> findByBookingId(int bookingId) {
        String sql = "SELECT bl.*, p.patient_name, c.center_name " +
            "FROM bills bl " +
            "JOIN bookings bk ON bl.booking_id = bk.booking_id " +
            "JOIN patients p  ON bk.patient_id = p.patient_id " +
            "JOIN centers c   ON bk.center_id  = c.center_id WHERE bl.booking_id = ?";
        return jdbc.query(sql, billRowMapper, bookingId).stream().findFirst();
    }

    /** Create a new bill. Total is computed server-side from booking_tests. */
    public int save(Bill bill) {
        String sql = "INSERT INTO bills (booking_id, total_amount, discount_amount, amount_paid, payment_status, payment_method, billed_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, bill.getBookingId());
            ps.setBigDecimal(2, bill.getTotalAmount());
            ps.setBigDecimal(3, bill.getDiscountAmount());
            ps.setBigDecimal(4, bill.getAmountPaid());
            ps.setString(5, bill.getPaymentStatus());
            ps.setString(6, bill.getPaymentMethod());
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    /** Record a payment (amount_paid + method + status update). */
    public void recordPayment(int billId, java.math.BigDecimal amountPaid,
                              String paymentMethod, String paymentStatus) {
        jdbc.update("UPDATE bills SET amount_paid=?, payment_method=?, payment_status=? WHERE bill_id=?",
                amountPaid, paymentMethod, paymentStatus, billId);
    }
}
