package com.diagnosticcenter.app.dao;

import com.diagnosticcenter.app.model.Doctor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/** DAO for doctors using JdbcTemplate (JDBC). */
@Repository
public class DoctorDao {

    private final JdbcTemplate jdbc;

    public DoctorDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Doctor> doctorRowMapper = (rs, rowNum) -> {
        Doctor d = new Doctor();
        d.setDoctorId(rs.getInt("doctor_id"));
        d.setDoctorName(rs.getString("doctor_name"));
        d.setSpecialization(rs.getString("specialization"));
        d.setPhone(rs.getString("phone"));
        d.setEmail(rs.getString("email"));
        int centerId = rs.getInt("center_id");
        if (!rs.wasNull()) d.setCenterId(centerId);
        d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        try { d.setCenterName(rs.getString("center_name")); } catch (Exception ignored) {}
        return d;
    };

    /** All doctors with their center name via LEFT JOIN. */
    public List<Doctor> findAll(String search, String specialization) {
        StringBuilder sql = new StringBuilder(
            "SELECT d.*, c.center_name FROM doctors d LEFT JOIN centers c ON d.center_id = c.center_id WHERE 1=1");
        List<Object> params = new java.util.ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append(" AND (d.doctor_name LIKE ? OR d.email LIKE ?)");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
        }
        if (specialization != null && !specialization.isBlank()) {
            sql.append(" AND d.specialization = ?");
            params.add(specialization);
        }
        sql.append(" ORDER BY d.doctor_name");
        return jdbc.query(sql.toString(), doctorRowMapper, params.toArray());
    }

    public Optional<Doctor> findById(int doctorId) {
        String sql = "SELECT d.*, c.center_name FROM doctors d LEFT JOIN centers c ON d.center_id = c.center_id " +
                     "WHERE d.doctor_id = ?";
        return jdbc.query(sql, doctorRowMapper, doctorId).stream().findFirst();
    }

    public int save(Doctor doctor) {
        String sql = "INSERT INTO doctors (doctor_name, specialization, phone, email, center_id) VALUES (?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, doctor.getDoctorName());
            ps.setString(2, doctor.getSpecialization());
            ps.setString(3, doctor.getPhone());
            ps.setString(4, doctor.getEmail());
            if (doctor.getCenterId() != null) ps.setInt(5, doctor.getCenterId());
            else ps.setNull(5, java.sql.Types.INTEGER);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public void update(Doctor doctor) {
        String sql = "UPDATE doctors SET doctor_name=?, specialization=?, phone=?, email=?, center_id=? WHERE doctor_id=?";
        jdbc.update(sql, doctor.getDoctorName(), doctor.getSpecialization(), doctor.getPhone(),
                    doctor.getEmail(), doctor.getCenterId(), doctor.getDoctorId());
    }

    public void delete(int doctorId) {
        jdbc.update("DELETE FROM doctors WHERE doctor_id = ?", doctorId);
    }

    /** Distinct specializations for filter dropdown. */
    public List<String> findAllSpecializations() {
        return jdbc.queryForList("SELECT DISTINCT specialization FROM doctors ORDER BY specialization", String.class);
    }

    /** Dropdown list filtered by center. */
    public List<Doctor> findByCenterId(int centerId) {
        return jdbc.query("SELECT d.*, c.center_name FROM doctors d LEFT JOIN centers c ON d.center_id = c.center_id WHERE d.center_id = ? ORDER BY d.doctor_name",
                doctorRowMapper, centerId);
    }
}
