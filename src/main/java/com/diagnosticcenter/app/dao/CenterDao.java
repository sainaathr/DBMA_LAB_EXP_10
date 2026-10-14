package com.diagnosticcenter.app.dao;

import com.diagnosticcenter.app.model.Center;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * DAO for diagnostic centers.
 * Uses JdbcTemplate with prepared statements — no SQL concatenation of user input.
 */
@Repository
public class CenterDao {

    private final JdbcTemplate jdbc;

    public CenterDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // RowMapper: ResultSet → Center
    private final RowMapper<Center> centerRowMapper = (rs, rowNum) -> {
        Center c = new Center();
        c.setCenterId(rs.getInt("center_id"));
        c.setCenterName(rs.getString("center_name"));
        c.setLocation(rs.getString("location"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        c.setCapacity(rs.getInt("capacity"));
        c.setManagerName(rs.getString("manager_name"));
        c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        // Optional join columns (present only when queried)
        try { c.setDoctorCount(rs.getLong("doctor_count")); } catch (Exception ignored) {}
        try { c.setBookingCount(rs.getLong("booking_count")); } catch (Exception ignored) {}
        return c;
    };

    /** Get all centers with doctor and booking counts. */
    public List<Center> findAll(String search) {
        String baseSQL =
            "SELECT c.*, " +
            "  (SELECT COUNT(*) FROM doctors d WHERE d.center_id = c.center_id) AS doctor_count, " +
            "  (SELECT COUNT(*) FROM bookings b WHERE b.center_id = c.center_id) AS booking_count " +
            "FROM centers c ";
        if (search != null && !search.isBlank()) {
            return jdbc.query(
                baseSQL + "WHERE c.center_name LIKE ? OR c.location LIKE ? ORDER BY c.center_name",
                centerRowMapper, "%" + search + "%", "%" + search + "%");
        }
        return jdbc.query(baseSQL + "ORDER BY c.center_name", centerRowMapper);
    }

    /** Find a single center by primary key. */
    public Optional<Center> findById(int centerId) {
        String sql = "SELECT c.*, " +
            "  (SELECT COUNT(*) FROM doctors d WHERE d.center_id = c.center_id) AS doctor_count, " +
            "  (SELECT COUNT(*) FROM bookings b WHERE b.center_id = c.center_id) AS booking_count " +
            "FROM centers c WHERE c.center_id = ?";
        return jdbc.query(sql, centerRowMapper, centerId).stream().findFirst();
    }

    /** INSERT new center — returns generated primary key. */
    public int save(Center center) {
        String sql = "INSERT INTO centers (center_name, location, phone, email, capacity, manager_name) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, center.getCenterName());
            ps.setString(2, center.getLocation());
            ps.setString(3, center.getPhone());
            ps.setString(4, center.getEmail());
            ps.setInt(5,    center.getCapacity());
            ps.setString(6, center.getManagerName());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    /** UPDATE existing center. */
    public void update(Center center) {
        String sql = "UPDATE centers SET center_name=?, location=?, phone=?, email=?, capacity=?, manager_name=? " +
                     "WHERE center_id=?";
        jdbc.update(sql, center.getCenterName(), center.getLocation(), center.getPhone(),
                    center.getEmail(), center.getCapacity(), center.getManagerName(), center.getCenterId());
    }

    /** DELETE center (will fail with FK violation if doctors/bookings exist). */
    public void delete(int centerId) {
        jdbc.update("DELETE FROM centers WHERE center_id = ?", centerId);
    }

    /** Light list (id + name) for dropdowns. */
    public List<Center> findAllForDropdown() {
        return jdbc.query("SELECT center_id, center_name, location, phone, '' AS email, 50 AS capacity, '' AS manager_name, NOW() AS created_at FROM centers ORDER BY center_name",
                centerRowMapper);
    }
}
