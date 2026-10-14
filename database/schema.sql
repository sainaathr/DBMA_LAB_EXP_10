-- ============================================================
-- Diagnostic Center Management System
-- Database Schema — MySQL
-- DBMS Lab PAT 4
-- ============================================================
-- Run this script as a MySQL root or admin user.
-- Creates the database, a dedicated application user,
-- all tables with FK constraints, indexes and sample data.
-- ============================================================

-- Create database
CREATE DATABASE IF NOT EXISTS diagnostic_center
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE diagnostic_center;

-- ── 1. USERS ─────────────────────────────────────────────
-- Stores application login accounts.
-- Passwords stored as BCrypt hashes (never plain text).
CREATE TABLE IF NOT EXISTS users (
  user_id    INT          NOT NULL AUTO_INCREMENT,
  username   VARCHAR(60)  NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  full_name  VARCHAR(120) NOT NULL,
  role       ENUM('ADMIN','STAFF') NOT NULL DEFAULT 'STAFF',
  created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id),
  UNIQUE KEY uq_users_username (username)
) ENGINE=InnoDB;

-- ── 2. CENTERS ───────────────────────────────────────────
-- Diagnostic center branches.
CREATE TABLE IF NOT EXISTS centers (
  center_id    INT          NOT NULL AUTO_INCREMENT,
  center_name  VARCHAR(150) NOT NULL,
  location     VARCHAR(255) NOT NULL,
  phone        VARCHAR(20)  NOT NULL,
  email        VARCHAR(100),
  capacity     INT          NOT NULL DEFAULT 50,
  manager_name VARCHAR(120),
  created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (center_id),
  KEY idx_centers_name (center_name)
) ENGINE=InnoDB;

-- ── 3. DOCTORS ───────────────────────────────────────────
-- Doctors assigned to diagnostic centers.
CREATE TABLE IF NOT EXISTS doctors (
  doctor_id      INT          NOT NULL AUTO_INCREMENT,
  doctor_name    VARCHAR(120) NOT NULL,
  specialization VARCHAR(100) NOT NULL,
  phone          VARCHAR(20)  NOT NULL,
  email          VARCHAR(100),
  center_id      INT,
  created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (doctor_id),
  -- FK: doctor → center.  RESTRICT prevents deleting a center that has doctors.
  CONSTRAINT fk_doctors_center
    FOREIGN KEY (center_id) REFERENCES centers (center_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  KEY idx_doctors_center (center_id),
  KEY idx_doctors_spec (specialization)
) ENGINE=InnoDB;

-- ── 4. PATIENTS ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS patients (
  patient_id   INT          NOT NULL AUTO_INCREMENT,
  patient_name VARCHAR(120) NOT NULL,
  date_of_birth DATE,
  gender       ENUM('Male','Female','Other') NOT NULL,
  phone        VARCHAR(20)  NOT NULL,
  email        VARCHAR(100),
  address      TEXT,
  created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (patient_id),
  KEY idx_patients_name (patient_name),
  KEY idx_patients_phone (phone)
) ENGINE=InnoDB;

-- ── 5. TESTS ─────────────────────────────────────────────
-- Diagnostic tests offered by the center.
CREATE TABLE IF NOT EXISTS tests (
  test_id          INT            NOT NULL AUTO_INCREMENT,
  test_name        VARCHAR(150)   NOT NULL,
  description      TEXT,
  price            DECIMAL(10,2)  NOT NULL,
  duration_minutes INT            NOT NULL DEFAULT 30,
  created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (test_id),
  KEY idx_tests_name (test_name),
  -- Business rule: price must be ≥ 0
  CONSTRAINT chk_tests_price CHECK (price >= 0),
  CONSTRAINT chk_tests_duration CHECK (duration_minutes > 0)
) ENGINE=InnoDB;

-- ── 6. BOOKINGS ──────────────────────────────────────────
-- One booking = one patient visit with a doctor at a center.
CREATE TABLE IF NOT EXISTS bookings (
  booking_id   INT          NOT NULL AUTO_INCREMENT,
  patient_id   INT          NOT NULL,
  doctor_id    INT,
  center_id    INT          NOT NULL,
  booking_date DATE         NOT NULL,
  booking_time TIME,
  status       ENUM('Pending','Confirmed','Completed','Cancelled') NOT NULL DEFAULT 'Pending',
  notes        TEXT,
  created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (booking_id),
  CONSTRAINT fk_bookings_patient
    FOREIGN KEY (patient_id) REFERENCES patients (patient_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_bookings_doctor
    FOREIGN KEY (doctor_id) REFERENCES doctors (doctor_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_bookings_center
    FOREIGN KEY (center_id) REFERENCES centers (center_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  KEY idx_bookings_patient (patient_id),
  KEY idx_bookings_date (booking_date),
  KEY idx_bookings_status (status)
) ENGINE=InnoDB;

-- ── 7. BOOKING_TESTS ─────────────────────────────────────
-- Many-to-many: one booking can include multiple tests.
-- Composite primary key on (booking_id, test_id).
CREATE TABLE IF NOT EXISTS booking_tests (
  booking_id   INT           NOT NULL,
  test_id      INT           NOT NULL,
  agreed_price DECIMAL(10,2) NOT NULL,   -- price at time of booking
  PRIMARY KEY (booking_id, test_id),
  CONSTRAINT fk_bt_booking
    FOREIGN KEY (booking_id) REFERENCES bookings (booking_id)
    ON UPDATE CASCADE ON DELETE CASCADE,  -- delete line items when booking deleted
  CONSTRAINT fk_bt_test
    FOREIGN KEY (test_id) REFERENCES tests (test_id)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ── 8. BILLS ─────────────────────────────────────────────
-- One bill per booking.
CREATE TABLE IF NOT EXISTS bills (
  bill_id        INT            NOT NULL AUTO_INCREMENT,
  booking_id     INT            NOT NULL,
  total_amount   DECIMAL(10,2)  NOT NULL,
  discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  amount_paid    DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
  payment_status ENUM('Unpaid','Partial','Paid') NOT NULL DEFAULT 'Unpaid',
  payment_method ENUM('Cash','Card','UPI','Insurance','Other') DEFAULT NULL,
  billed_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (bill_id),
  CONSTRAINT fk_bills_booking
    FOREIGN KEY (booking_id) REFERENCES bookings (booking_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  UNIQUE KEY uq_bills_booking (booking_id),  -- one bill per booking
  CONSTRAINT chk_bills_total    CHECK (total_amount >= 0),
  CONSTRAINT chk_bills_discount CHECK (discount_amount >= 0),
  CONSTRAINT chk_bills_paid     CHECK (amount_paid >= 0)
) ENGINE=InnoDB;
