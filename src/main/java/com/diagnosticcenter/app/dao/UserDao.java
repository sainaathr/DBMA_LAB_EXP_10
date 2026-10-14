package com.diagnosticcenter.app.dao;

import com.diagnosticcenter.app.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * DAO for application users.
 * Uses JdbcTemplate (JDBC — mandatory academic requirement).
 * All SQL uses parameterised queries to prevent SQL injection.
 */
@Repository
public class UserDao {

    private final JdbcTemplate jdbc;

    public UserDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // RowMapper: maps a ResultSet row to a User object
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setRole(rs.getString("role"));
        u.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return u;
    };

    /** Find a user by username — used by Spring Security. */
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        return jdbc.query(sql, userRowMapper, username).stream().findFirst();
    }

    /** Find a user by primary key. */
    public Optional<User> findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        return jdbc.query(sql, userRowMapper, userId).stream().findFirst();
    }
}
