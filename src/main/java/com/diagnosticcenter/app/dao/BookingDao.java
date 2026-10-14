package com.diagnosticcenter.app.dao;

import com.diagnosticcenter.app.model.Booking;
import com.diagnosticcenter.app.model.BookingTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for bookings and their test line items.
 * Uses @Transactional on multi-table write operations.
 */
@Repository
public class BookingDao {

    private final JdbcTemplate jdbc;

    public BookingDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Booking> bookingRowMapper = (rs, rowNum) -> {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        b.setPatientId(rs.getInt("patient_id"));
        int docId = rs.getInt("doctor_id"); if (!rs.wasNull()) b.setDoctorId(docId);
        b.setCenterId(rs.getInt("center_id"));
        b.setBookingDate(rs.getDate("booking_date").toLocalDate());
        Time t = rs.getTime("booking_time"); if (t != null) b.setBookingTime(t.toLocalTime());
        b.setStatus(rs.getString("status"));
        b.setNotes(rs.getString("notes"));
        b.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        try { b.setPatientName(rs.getString("patient_name")); } catch (Exception ignored) {}
        try { b.setDoctorName(rs.getString("doctor_name")); } catch (Exception ignored) {}
        try { b.setCenterName(rs.getString("center_name")); } catch (Exception ignored) {}
        return b;
    };

    private final RowMapper<BookingTest> bookingTestRowMapper = (rs, rowNum) -> {
        BookingTest bt = new BookingTest();
        bt.setBookingId(rs.getInt("booking_id"));
        bt.setTestId(rs.getInt("test_id"));
        bt.setAgreedPrice(rs.getBigDecimal("agreed_price"));
        try { bt.setTestName(rs.getString("test_name")); } catch (Exception ignored) {}
        try { bt.setDurationMinutes(rs.getInt("duration_minutes")); } catch (Exception ignored) {}
        return bt;
    };

    /** Paginated booking list with JOIN. */
    public List<Booking> findAll(String search, String status, int offset, int limit) {
        StringBuilder sql = new StringBuilder(
            "SELECT bk.*, p.patient_name, d.doctor_name, c.center_name " +
            "FROM bookings bk " +
            "JOIN patients p ON bk.patient_id = p.patient_id " +
            "LEFT JOIN doctors d ON bk.doctor_id = d.doctor_id " +
            "JOIN centers c ON bk.center_id = c.center_id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append(" AND (p.patient_name LIKE ? OR CAST(bk.booking_id AS CHAR) = ?)");
            params.add("%" + search + "%"); params.add(search.trim());
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND bk.status = ?"); params.add(status);
        }
        sql.append(" ORDER BY bk.booking_date DESC LIMIT ? OFFSET ?");
        params.add(limit); params.add(offset);
        return jdbc.query(sql.toString(), bookingRowMapper, params.toArray());
    }

    public int countAll(String search, String status) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM bookings bk JOIN patients p ON bk.patient_id = p.patient_id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append(" AND (p.patient_name LIKE ? OR CAST(bk.booking_id AS CHAR) = ?)");
            params.add("%" + search + "%"); params.add(search.trim());
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND bk.status = ?"); params.add(status);
        }
        return jdbc.queryForObject(sql.toString(), Integer.class, params.toArray());
    }

    public Optional<Booking> findById(int bookingId) {
        String sql = "SELECT bk.*, p.patient_name, d.doctor_name, c.center_name " +
            "FROM bookings bk JOIN patients p ON bk.patient_id = p.patient_id " +
            "LEFT JOIN doctors d ON bk.doctor_id = d.doctor_id " +
            "JOIN centers c ON bk.center_id = c.center_id WHERE bk.booking_id = ?";
        Optional<Booking> booking = jdbc.query(sql, bookingRowMapper, bookingId).stream().findFirst();
        booking.ifPresent(b -> b.setTests(findTestsByBookingId(bookingId)));
        return booking;
    }

    public List<BookingTest> findTestsByBookingId(int bookingId) {
        String sql = "SELECT bt.*, t.test_name, t.duration_minutes FROM booking_tests bt " +
                     "JOIN tests t ON bt.test_id = t.test_id WHERE bt.booking_id = ?";
        return jdbc.query(sql, bookingTestRowMapper, bookingId);
    }

    /**
     * Save booking + its test line items in a single transaction.
     * Returns the generated booking_id.
     */
    @Transactional
    public int save(Booking booking, List<com.diagnosticcenter.app.model.Test> selectedTests) {
        String sql = "INSERT INTO bookings (patient_id, doctor_id, center_id, booking_date, booking_time, status, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, booking.getPatientId());
            if (booking.getDoctorId() != null) ps.setInt(2, booking.getDoctorId());
            else ps.setNull(2, java.sql.Types.INTEGER);
            ps.setInt(3, booking.getCenterId());
            ps.setDate(4, Date.valueOf(booking.getBookingDate()));
            if (booking.getBookingTime() != null) ps.setTime(5, Time.valueOf(booking.getBookingTime()));
            else ps.setNull(5, java.sql.Types.TIME);
            ps.setString(6, booking.getStatus() != null ? booking.getStatus() : "Pending");
            ps.setString(7, booking.getNotes());
            return ps;
        }, keyHolder);

        int newId = keyHolder.getKey().intValue();

        // Insert booking_tests rows (one per selected test)
        for (com.diagnosticcenter.app.model.Test test : selectedTests) {
            jdbc.update("INSERT INTO booking_tests (booking_id, test_id, agreed_price) VALUES (?, ?, ?)",
                    newId, test.getTestId(), test.getPrice());
        }
        return newId;
    }

    /** Update only the booking status. */
    public void updateStatus(int bookingId, String status) {
        jdbc.update("UPDATE bookings SET status = ? WHERE booking_id = ?", status, bookingId);
    }

    public void delete(int bookingId) {
        // booking_tests rows cascade-deleted automatically by FK
        jdbc.update("DELETE FROM bookings WHERE booking_id = ?", bookingId);
    }

    /** Recent bookings for dashboard widget. */
    public List<Booking> findRecent(int limit) {
        String sql = "SELECT bk.*, p.patient_name, d.doctor_name, c.center_name " +
            "FROM bookings bk JOIN patients p ON bk.patient_id = p.patient_id " +
            "LEFT JOIN doctors d ON bk.doctor_id = d.doctor_id " +
            "JOIN centers c ON bk.center_id = c.center_id " +
            "ORDER BY bk.created_at DESC LIMIT ?";
        return jdbc.query(sql, bookingRowMapper, limit);
    }
}
