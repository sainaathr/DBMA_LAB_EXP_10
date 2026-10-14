package com.diagnosticcenter.app.service;

import com.diagnosticcenter.app.dao.*;
import com.diagnosticcenter.app.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer — business logic, validation, and DAO orchestration.
 * Controllers call services; services call DAOs.
 */
@Service
public class DiagnosticService {

    private final PatientDao patientDao;
    private final DoctorDao  doctorDao;
    private final CenterDao  centerDao;
    private final TestDao    testDao;
    private final BookingDao bookingDao;
    private final BillDao    billDao;
    private final DashboardDao dashboardDao;

    public DiagnosticService(PatientDao patientDao, DoctorDao doctorDao, CenterDao centerDao,
                              TestDao testDao, BookingDao bookingDao, BillDao billDao,
                              DashboardDao dashboardDao) {
        this.patientDao  = patientDao;
        this.doctorDao   = doctorDao;
        this.centerDao   = centerDao;
        this.testDao     = testDao;
        this.bookingDao  = bookingDao;
        this.billDao     = billDao;
        this.dashboardDao = dashboardDao;
    }

    // ─── Dashboard ────────────────────────────────────────────────────────────

    public DashboardStats getDashboardStats()                  { return dashboardDao.getStats(); }
    public List<Map<String, Object>> getBookingTrend()        { return dashboardDao.getBookingTrend(); }
    public List<Map<String, Object>> getRevenueTrend()        { return dashboardDao.getRevenueTrend(); }
    public Map<String, Long> getBookingStatusDistribution()   { return dashboardDao.getBookingStatusDistribution(); }
    public List<Booking> getRecentBookings()                   { return bookingDao.findRecent(8); }

    // ─── Patients ─────────────────────────────────────────────────────────────

    public List<Patient> getPatients(String search, int page, int size) {
        return patientDao.findAll(search, (page - 1) * size, size);
    }
    public int countPatients(String search)             { return patientDao.countAll(search); }
    public Optional<Patient> getPatient(int id)         { return patientDao.findById(id); }
    public int createPatient(Patient p)                  { return patientDao.save(p); }
    public void updatePatient(Patient p)                 { patientDao.update(p); }
    public void deletePatient(int id)                    { patientDao.delete(id); }

    // ─── Doctors ──────────────────────────────────────────────────────────────

    public List<Doctor> getDoctors(String search, String spec) { return doctorDao.findAll(search, spec); }
    public Optional<Doctor> getDoctor(int id)                  { return doctorDao.findById(id); }
    public int createDoctor(Doctor d)                          { return doctorDao.save(d); }
    public void updateDoctor(Doctor d)                         { doctorDao.update(d); }
    public void deleteDoctor(int id)                           { doctorDao.delete(id); }
    public List<String> getSpecializations()                   { return doctorDao.findAllSpecializations(); }
    public List<Doctor> getDoctorsByCenter(int centerId)       { return doctorDao.findByCenterId(centerId); }

    // ─── Centers ──────────────────────────────────────────────────────────────

    public List<Center> getCenters(String search)       { return centerDao.findAll(search); }
    public Optional<Center> getCenter(int id)           { return centerDao.findById(id); }
    public int createCenter(Center c)                   { return centerDao.save(c); }
    public void updateCenter(Center c)                  { centerDao.update(c); }
    public void deleteCenter(int id)                    { centerDao.delete(id); }
    public List<Center> getCentersForDropdown()         { return centerDao.findAllForDropdown(); }

    // ─── Tests ────────────────────────────────────────────────────────────────

    public List<Test> getTests(String search)           { return testDao.findAll(search); }
    public Optional<Test> getTest(int id)               { return testDao.findById(id); }
    public int createTest(Test t)                       { return testDao.save(t); }
    public void updateTest(Test t)                      { testDao.update(t); }
    public void deleteTest(int id)                      { testDao.delete(id); }

    // ─── Bookings ─────────────────────────────────────────────────────────────

    public List<Booking> getBookings(String search, String status, int page, int size) {
        return bookingDao.findAll(search, status, (page - 1) * size, size);
    }
    public int countBookings(String search, String status) { return bookingDao.countAll(search, status); }
    public Optional<Booking> getBooking(int id)            { return bookingDao.findById(id); }
    public void updateBookingStatus(int id, String status)  { bookingDao.updateStatus(id, status); }
    public void deleteBooking(int id)                       { bookingDao.delete(id); }

    /**
     * Create a booking with selected tests.
     * Server-side: total is calculated from DB test prices — client totals are ignored.
     */
    @Transactional
    public int createBooking(Booking booking) {
        // Fetch test prices from DB (not from client)
        List<Test> selectedTests = testDao.findByIds(booking.getTestIds());
        if (selectedTests.isEmpty()) {
            throw new IllegalArgumentException("No valid tests selected.");
        }
        return bookingDao.save(booking, selectedTests);
    }

    // ─── Bills ────────────────────────────────────────────────────────────────

    public List<Bill> getBills(String status, int page, int size) {
        return billDao.findAll(status, (page - 1) * size, size);
    }
    public int countBills(String status)               { return billDao.countAll(status); }
    public Optional<Bill> getBill(int id)              { return billDao.findById(id); }
    public Optional<Bill> getBillByBookingId(int id)   { return billDao.findByBookingId(id); }

    /**
     * Generate a bill for a booking.
     * Total is calculated server-side from booking_tests (agreed_price at time of booking).
     */
    @Transactional
    public int generateBill(int bookingId, BigDecimal discountAmount) {
        // Verify no bill exists
        if (billDao.findByBookingId(bookingId).isPresent()) {
            throw new IllegalStateException("A bill already exists for booking #" + bookingId);
        }
        // Compute total from DB
        List<BookingTest> items = bookingDao.findTestsByBookingId(bookingId);
        BigDecimal total = items.stream()
                .map(BookingTest::getAgreedPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;
        if (discountAmount.compareTo(total) > 0) discountAmount = total;

        Bill bill = new Bill();
        bill.setBookingId(bookingId);
        bill.setTotalAmount(total);
        bill.setDiscountAmount(discountAmount);
        bill.setAmountPaid(BigDecimal.ZERO);
        bill.setPaymentStatus("Unpaid");
        return billDao.save(bill);
    }

    /**
     * Record a payment for a bill.
     * Validates that payment amount ≤ outstanding balance (server-side).
     */
    @Transactional
    public void recordPayment(int billId, BigDecimal paymentAmount, String paymentMethod) {
        Bill bill = billDao.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found: " + billId));
        if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }
        BigDecimal outstanding = bill.getOutstandingAmount();
        BigDecimal newPaid = bill.getAmountPaid().add(paymentAmount);
        if (newPaid.compareTo(bill.getTotalAmount().subtract(bill.getDiscountAmount())) > 0) {
            throw new IllegalArgumentException("Payment exceeds outstanding balance.");
        }
        String status = newPaid.compareTo(bill.getTotalAmount().subtract(bill.getDiscountAmount())) >= 0
                ? "Paid" : "Partial";
        billDao.recordPayment(billId, newPaid, paymentMethod, status);
    }
}
