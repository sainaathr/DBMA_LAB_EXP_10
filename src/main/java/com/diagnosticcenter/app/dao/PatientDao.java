package com.diagnosticcenter.app.dao;

import com.diagnosticcenter.app.model.Patient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO for patients using JdbcTemplate (JDBC). */
@Repository
public class PatientDao {

    private final JdbcTemplate jdbc;

    public PatientDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Patient> patientRowMapper = (rs, rowNum) -> {
        Patient p = new Patient();
        p.setPatientId(rs.getInt("patient_id"));
        p.setPatientName(rs.getString("patient_name"));
        Date dob = rs.getDate("date_of_birth");
        if (dob != null) p.setDateOfBirth(dob.toLocalDate());
        p.setGender(rs.getString("gender"));
        p.setPhone(rs.getString("phone"));
        p.setEmail(rs.getString("email"));
        p.setAddress(rs.getString("address"));
        p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return p;
    };

    /** Paginated patient list with optional search. */
    public List<Patient> findAll(String search, int offset, int limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM patients WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append(" AND (patient_name LIKE ? OR phone LIKE ? OR CAST(patient_id AS CHAR) = ?)");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
            params.add(search.trim());
        }
        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return jdbc.query(sql.toString(), patientRowMapper, params.toArray());
    }

    /** Total count (for pagination maths). */
    public int countAll(String search) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM patients WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append(" AND (patient_name LIKE ? OR phone LIKE ? OR CAST(patient_id AS CHAR) = ?)");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
            params.add(search.trim());
        }
        return jdbc.queryForObject(sql.toString(), Integer.class, params.toArray());
    }

    public Optional<Patient> findById(int patientId) {
        return jdbc.query("SELECT * FROM patients WHERE patient_id = ?", patientRowMapper, patientId)
                   .stream().findFirst();
    }

    public int save(Patient patient) {
        String sql = "INSERT INTO patients (patient_name, date_of_birth, gender, phone, email, address) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, patient.getPatientName());
            ps.setDate(2, patient.getDateOfBirth() != null ? Date.valueOf(patient.getDateOfBirth()) : null);
            ps.setString(3, patient.getGender());
            ps.setString(4, patient.getPhone());
            ps.setString(5, patient.getEmail());
            ps.setString(6, patient.getAddress());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public void update(Patient patient) {
        String sql = "UPDATE patients SET patient_name=?, date_of_birth=?, gender=?, phone=?, email=?, address=? " +
                     "WHERE patient_id=?";
        jdbc.update(sql, patient.getPatientName(),
                patient.getDateOfBirth() != null ? Date.valueOf(patient.getDateOfBirth()) : null,
                patient.getGender(), patient.getPhone(), patient.getEmail(), patient.getAddress(),
                patient.getPatientId());
    }

    public void delete(int patientId) {
        jdbc.update("DELETE FROM patients WHERE patient_id = ?", patientId);
    }
}
