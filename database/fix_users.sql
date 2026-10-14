DELETE FROM users WHERE username IN ('admin','staff');
INSERT INTO users (username, password_hash, full_name, role) VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTIEmyiHhHe', 'System Administrator', 'ADMIN');
INSERT INTO users (username, password_hash, full_name, role) VALUES ('staff', '$2a$10$eVJ8GrKuBHKcGbF3N5fMDOdNWQFjFZE6QBYv5l7dF3V6U4N.4L1O2', 'Staff User', 'STAFF');
SELECT username, role FROM users;
