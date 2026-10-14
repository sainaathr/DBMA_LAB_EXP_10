package com.diagnosticcenter.app.controller;

import com.diagnosticcenter.app.dao.BookingDao;
import com.diagnosticcenter.app.dao.BillDao;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Reports page: date-filtered aggregate SQL queries for bookings and revenue. */
@Controller
@RequestMapping("/reports")
public class ReportController {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public ReportController(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String reports(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model) throws JsonProcessingException {

        // Default: last 30 days
        if (from == null) from = LocalDate.now().minusDays(30);
        if (to   == null) to   = LocalDate.now();

        // ─── Bookings by date ─────────────────────────────────────────────────
        String bookingSQL =
            "SELECT DATE(booking_date) AS day, COUNT(*) AS total " +
            "FROM bookings WHERE booking_date BETWEEN ? AND ? " +
            "GROUP BY day ORDER BY day";
        List<Map<String, Object>> bookingRows = jdbc.queryForList(bookingSQL, from, to);

        List<String> bookLabels = new ArrayList<>();
        List<Long>   bookData   = new ArrayList<>();
        for (Map<String, Object> r : bookingRows) {
            bookLabels.add(r.get("day").toString());
            bookData.add(((Number) r.get("total")).longValue());
        }
        model.addAttribute("bookLabels", objectMapper.writeValueAsString(bookLabels));
        model.addAttribute("bookData",   objectMapper.writeValueAsString(bookData));

        // ─── Revenue by date ──────────────────────────────────────────────────
        String revenueSQL =
            "SELECT DATE(bl.billed_at) AS day, " +
            "  SUM(bl.amount_paid) AS revenue, " +
            "  SUM(bl.total_amount - bl.discount_amount - bl.amount_paid) AS outstanding " +
            "FROM bills bl WHERE DATE(bl.billed_at) BETWEEN ? AND ? " +
            "GROUP BY day ORDER BY day";
        List<Map<String, Object>> revenueRows = jdbc.queryForList(revenueSQL, from, to);
        model.addAttribute("revenueRows", revenueRows);

        List<String> revLabels = new ArrayList<>();
        List<Double> revData   = new ArrayList<>();
        for (Map<String, Object> r : revenueRows) {
            revLabels.add(r.get("day").toString());
            revData.add(r.get("revenue") != null ? ((Number) r.get("revenue")).doubleValue() : 0.0);
        }
        model.addAttribute("revLabels", objectMapper.writeValueAsString(revLabels));
        model.addAttribute("revData",   objectMapper.writeValueAsString(revData));

        // ─── Summary totals ───────────────────────────────────────────────────
        String summarySQL =
            "SELECT COUNT(*) AS total_bookings, " +
            "  SUM(CASE WHEN status='Completed' THEN 1 ELSE 0 END) AS completed, " +
            "  SUM(CASE WHEN status='Pending'   THEN 1 ELSE 0 END) AS pending " +
            "FROM bookings WHERE booking_date BETWEEN ? AND ?";
        Map<String, Object> summary = jdbc.queryForMap(summarySQL, from, to);
        model.addAttribute("summary", summary);

        model.addAttribute("from", from);
        model.addAttribute("to",   to);
        model.addAttribute("pageTitle", "Reports & Analytics");
        model.addAttribute("activePage", "reports");
        return "reports/index";
    }
}
