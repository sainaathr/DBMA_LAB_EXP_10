package com.diagnosticcenter.app.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * Represents an application user (login account).
 * Passwords are stored as BCrypt hashes — plain text is never stored.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String role;           // "ADMIN" or "STAFF"
    private LocalDateTime createdAt;
}
