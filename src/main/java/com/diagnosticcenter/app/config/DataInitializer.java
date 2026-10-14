package com.diagnosticcenter.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Runs once at startup to ensure the demo admin/staff accounts
 * exist with correctly BCrypt-hashed passwords.
 *
 * This is idempotent: it uses INSERT … ON DUPLICATE KEY UPDATE
 * so it is safe to run multiple times.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;

    public DataInitializer(JdbcTemplate jdbc, PasswordEncoder encoder) {
        this.jdbc    = jdbc;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        upsertUser("admin", "Admin@1234", "System Administrator", "ADMIN");
        upsertUser("staff", "Staff@1234", "Lab Staff Member",      "STAFF");
        log.info("✅ Demo accounts ready  →  admin / Admin@1234  |  staff / Staff@1234");
    }

    private void upsertUser(String username, String rawPassword,
                            String fullName, String role) {
        String hash = encoder.encode(rawPassword);
        int updated = jdbc.update(
            "UPDATE users SET password_hash = ?, full_name = ?, role = ? WHERE username = ?",
            hash, fullName, role, username);
        if (updated == 0) {
            jdbc.update(
                "INSERT INTO users (username, password_hash, full_name, role) VALUES (?, ?, ?, ?)",
                username, hash, fullName, role);
        }
        log.info("  account '{}' ({}) ensured.", username, role);
    }
}
