-- ============================================================
-- Diagnostic Center Management System
-- Seed Data — Demo / Development Records
-- DBMS Lab PAT 4
-- ============================================================
-- Run after schema.sql.
-- All patient data is fictional and used only for demonstration.
-- ============================================================

USE diagnostic_center;

-- ── Application users ─────────────────────────────────────
-- Password for BOTH accounts is:  Admin@1234
-- Hash generated with BCrypt (cost 10).
INSERT INTO users (username, password_hash, full_name, role) VALUES
('admin',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LnoQ.D6Q6Mm',
 'System Administrator', 'ADMIN'),
('staff',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LnoQ.D6Q6Mm',
 'Lab Staff Member', 'STAFF')
ON DUPLICATE KEY UPDATE user_id = user_id;

-- ── Diagnostic Centers ────────────────────────────────────
INSERT INTO centers (center_name, location, phone, email, capacity, manager_name) VALUES
('MedScan Central', '12, MG Road, Bengaluru 560001', '+91-80-2345-6789', 'central@medscan.in', 100, 'Dr. Ramesh Kumar'),
('HealthPath East', '45, Park Street, Kolkata 700016', '+91-33-4567-8901', 'east@healthpath.in', 80, 'Dr. Priya Sen'),
('ClearView Diagnostics', '7, Linking Road, Mumbai 400050', '+91-22-6789-0123', 'info@clearview.in', 120, 'Dr. Anjali Mehta'),
('LifeCare Labs', '23, Anna Salai, Chennai 600002', '+91-44-2345-9876', 'support@lifecare.in', 90, 'Dr. Suresh Babu')
ON DUPLICATE KEY UPDATE center_id = center_id;

-- ── Doctors ───────────────────────────────────────────────
INSERT INTO doctors (doctor_name, specialization, phone, email, center_id) VALUES
('Dr. Anil Sharma',    'Radiology',         '+91-98100-11111', 'anil.sharma@medscan.in',  1),
('Dr. Sunita Rao',     'Pathology',         '+91-98200-22222', 'sunita.rao@medscan.in',   1),
('Dr. Vikram Nair',    'Cardiology',        '+91-98300-33333', 'vikram@healthpath.in',    2),
('Dr. Meena Iyer',     'Neurology',         '+91-98400-44444', 'meena@healthpath.in',     2),
('Dr. Rajesh Patel',   'General Medicine',  '+91-98500-55555', 'rajesh@clearview.in',     3),
('Dr. Kavitha Menon',  'Biochemistry',      '+91-98600-66666', 'kavitha@clearview.in',    3),
('Dr. Sanjay Gupta',   'Ultrasound',        '+91-98700-77777', 'sanjay@lifecare.in',      4),
('Dr. Divya Krishnan', 'Endocrinology',     '+91-98800-88888', 'divya@lifecare.in',       4)
ON DUPLICATE KEY UPDATE doctor_id = doctor_id;

-- ── Patients (fictional names only) ──────────────────────
INSERT INTO patients (patient_name, date_of_birth, gender, phone, email, address) VALUES
('Rahul Verma',       '1985-03-15', 'Male',   '+91-99001-00001', 'rahul.v@email.com',   '14, Rose Garden, Bengaluru'),
('Priya Mehta',       '1992-07-22', 'Female', '+91-99002-00002', 'priya.m@email.com',   '8, Elm Street, Mumbai'),
('Arjun Singh',       '1978-11-30', 'Male',   '+91-99003-00003', 'arjun.s@email.com',   '22, Oak Avenue, Delhi'),
('Sneha Pillai',      '1990-05-10', 'Female', '+91-99004-00004', 'sneha.p@email.com',   '5, Lake View, Kochi'),
('Manoj Kumar',       '1965-01-25', 'Male',   '+91-99005-00005', 'manoj.k@email.com',   '33, Hill Road, Pune'),
('Anita Desai',       '1998-09-08', 'Female', '+91-99006-00006', 'anita.d@email.com',   '7, Palace Road, Mysuru'),
('Kiran Reddy',       '1980-04-17', 'Male',   '+91-99007-00007', 'kiran.r@email.com',   '11, Green Lane, Hyderabad'),
('Deepa Nair',        '1975-12-03', 'Female', '+91-99008-00008', 'deepa.n@email.com',   '19, Sunset Blvd, Chennai'),
('Suresh Babu',       '1988-06-21', 'Male',   '+91-99009-00009', 'suresh.b@email.com',  'Flat 3, Silver Oaks, Bengaluru'),
('Lakshmi Venkat',    '1995-02-14', 'Female', '+91-99010-00010', 'lakshmi.v@email.com', '6, Harmony Enclave, Coimbatore')
ON DUPLICATE KEY UPDATE patient_id = patient_id;

-- ── Diagnostic Tests ─────────────────────────────────────
INSERT INTO tests (test_name, description, price, duration_minutes) VALUES
('Complete Blood Count (CBC)',       'Full blood count including WBC, RBC, platelets and hemoglobin.', 350.00, 30),
('Blood Glucose Fasting',            'Measures fasting blood glucose levels for diabetes screening.',   150.00, 15),
('Lipid Profile',                    'Cholesterol, HDL, LDL and triglycerides panel.',                 550.00, 30),
('Thyroid Profile (T3/T4/TSH)',      'Complete thyroid function test.',                                  650.00, 45),
('Liver Function Test (LFT)',        'ALT, AST, bilirubin and other liver enzymes.',                    500.00, 30),
('Kidney Function Test (KFT)',       'Creatinine, urea and electrolyte panel.',                         500.00, 30),
('Urine Routine Examination',        'Microscopic and chemical analysis of urine sample.',             200.00, 20),
('Chest X-Ray',                      'Standard PA view chest radiograph.',                              400.00, 15),
('ECG (Electrocardiogram)',          '12-lead resting ECG for cardiac evaluation.',                     300.00, 20),
('Ultrasound Abdomen',               'Sonography of abdominal organs.',                                 800.00, 45),
('HbA1c (Glycated Haemoglobin)',    '3-month average blood sugar control indicator.',                   400.00, 20),
('COVID-19 RT-PCR',                  'Real-time PCR test for SARS-CoV-2 detection.',                  1200.00, 60),
('Vitamin D (25-OH)',                'Serum Vitamin D level assessment.',                               700.00, 20),
('Iron Studies Panel',               'Serum iron, TIBC and ferritin levels.',                           600.00, 30),
('MRI Brain (without contrast)',     'Magnetic resonance imaging of the brain.',                       4500.00, 90)
ON DUPLICATE KEY UPDATE test_id = test_id;

-- ── Bookings ─────────────────────────────────────────────
INSERT INTO bookings (patient_id, doctor_id, center_id, booking_date, booking_time, status, notes) VALUES
(1, 1, 1, '2026-10-01', '09:00:00', 'Completed', 'Routine annual check-up'),
(2, 2, 1, '2026-10-02', '10:30:00', 'Completed', 'Follow-up for anaemia'),
(3, 3, 2, '2026-10-05', '11:00:00', 'Completed', 'Cardiac screening'),
(4, 4, 2, '2026-10-07', '09:30:00', 'Confirmed', 'Thyroid disorder monitoring'),
(5, 5, 3, '2026-10-10', '14:00:00', 'Pending',   'Diabetes follow-up'),
(6, 6, 3, '2026-10-11', '11:30:00', 'Pending',   'Liver function check'),
(7, 7, 4, '2026-10-12', '10:00:00', 'Confirmed', 'Abdominal pain evaluation'),
(8, 8, 4, '2026-10-13', '15:00:00', 'Pending',   'Thyroid and Vitamin D check'),
(9, 1, 1, '2026-10-14', '09:00:00', 'Pending',   'Pre-employment health check'),
(10,2, 1, '2026-10-15', '10:00:00', 'Pending',   'Comprehensive blood work');

-- ── Booking Tests ─────────────────────────────────────────
-- Booking 1: CBC + Lipid Profile
INSERT INTO booking_tests (booking_id, test_id, agreed_price) VALUES
(1, 1, 350.00), (1, 3, 550.00);

-- Booking 2: CBC + Urine Routine
INSERT INTO booking_tests (booking_id, test_id, agreed_price) VALUES
(2, 1, 350.00), (2, 7, 200.00);

-- Booking 3: ECG + Lipid Profile
INSERT INTO booking_tests (booking_id, test_id, agreed_price) VALUES
(3, 9, 300.00), (3, 3, 550.00);

-- Booking 4: Thyroid Profile + HbA1c
INSERT INTO booking_tests (booking_id, test_id, agreed_price) VALUES
(4, 4, 650.00), (4, 11, 400.00);

-- Booking 5: Blood Glucose + HbA1c
INSERT INTO booking_tests (booking_id, test_id, agreed_price) VALUES
(5, 2, 150.00), (5, 11, 400.00);

-- Booking 6: LFT + KFT
INSERT INTO booking_tests (booking_id, test_id, agreed_price) VALUES
(6, 5, 500.00), (6, 6, 500.00);

-- Booking 7: Ultrasound Abdomen
INSERT INTO booking_tests (booking_id, test_id, agreed_price) VALUES
(7, 10, 800.00);

-- Booking 8: Thyroid Profile + Vitamin D + Iron Studies
INSERT INTO booking_tests (booking_id, test_id, agreed_price) VALUES
(8, 4, 650.00), (8, 13, 700.00), (8, 14, 600.00);

-- Booking 9: CBC + Chest X-Ray + ECG
INSERT INTO booking_tests (booking_id, test_id, agreed_price) VALUES
(9, 1, 350.00), (9, 8, 400.00), (9, 9, 300.00);

-- Booking 10: CBC + Lipid Profile + LFT + KFT
INSERT INTO booking_tests (booking_id, test_id, agreed_price) VALUES
(10, 1, 350.00), (10, 3, 550.00), (10, 5, 500.00), (10, 6, 500.00);

-- ── Bills ─────────────────────────────────────────────────
-- Completed bookings have paid bills; others are unpaid.
INSERT INTO bills (booking_id, total_amount, discount_amount, amount_paid, payment_status, payment_method) VALUES
(1, 900.00,  50.00,  850.00, 'Paid',    'Card'),
(2, 550.00,   0.00,  550.00, 'Paid',    'Cash'),
(3, 850.00, 100.00,  750.00, 'Paid',    'UPI'),
(4, 1050.00,  0.00,  500.00, 'Partial', 'Cash'),
(5, 550.00,   0.00,    0.00, 'Unpaid',  NULL),
(6, 1000.00,  0.00,    0.00, 'Unpaid',  NULL),
(7, 800.00,   0.00,    0.00, 'Unpaid',  NULL);
