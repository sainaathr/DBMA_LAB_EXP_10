package com.diagnosticcenter.app.dao;

import com.diagnosticcenter.app.model.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO for diagnostic tests using JdbcTemplate (JDBC). */
@Repository
public class TestDao {

    private final JdbcTemplate jdbc;

    public TestDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Test> testRowMapper = (rs, rowNum) -> {
        Test t = new Test();
        t.setTestId(rs.getInt("test_id"));
        t.setTestName(rs.getString("test_name"));
        t.setDescription(rs.getString("description"));
        t.setPrice(rs.getBigDecimal("price"));
        t.setDurationMinutes(rs.getInt("duration_minutes"));
        t.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return t;
    };

    public List<Test> findAll(String search) {
        StringBuilder sql = new StringBuilder("SELECT * FROM tests WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append(" AND test_name LIKE ?");
            params.add("%" + search + "%");
        }
        sql.append(" ORDER BY test_name");
        return jdbc.query(sql.toString(), testRowMapper, params.toArray());
    }

    public Optional<Test> findById(int testId) {
        return jdbc.query("SELECT * FROM tests WHERE test_id = ?", testRowMapper, testId)
                   .stream().findFirst();
    }

    /** Find multiple tests by their IDs (for booking total calculation). */
    public List<Test> findByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        String placeholders = String.join(",", ids.stream().map(i -> "?").toList());
        return jdbc.query("SELECT * FROM tests WHERE test_id IN (" + placeholders + ")",
                testRowMapper, ids.toArray());
    }

    public int save(Test test) {
        String sql = "INSERT INTO tests (test_name, description, price, duration_minutes) VALUES (?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, test.getTestName());
            ps.setString(2, test.getDescription());
            ps.setBigDecimal(3, test.getPrice());
            ps.setInt(4, test.getDurationMinutes());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public void update(Test test) {
        jdbc.update("UPDATE tests SET test_name=?, description=?, price=?, duration_minutes=? WHERE test_id=?",
                test.getTestName(), test.getDescription(), test.getPrice(),
                test.getDurationMinutes(), test.getTestId());
    }

    public void delete(int testId) {
        jdbc.update("DELETE FROM tests WHERE test_id = ?", testId);
    }
}
