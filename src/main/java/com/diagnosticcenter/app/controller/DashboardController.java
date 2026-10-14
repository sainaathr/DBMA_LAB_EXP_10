package com.diagnosticcenter.app.controller;

import com.diagnosticcenter.app.model.DashboardStats;
import com.diagnosticcenter.app.service.DiagnosticService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Dashboard — all metrics fetched live from MySQL via service/DAO layer. */
@Controller
public class DashboardController {

    private final DiagnosticService service;
    private final ObjectMapper objectMapper;

    public DashboardController(DiagnosticService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) throws JsonProcessingException {
        // ─── Stats cards ────────────────────────────────────────────────────────
        DashboardStats stats = service.getDashboardStats();
        model.addAttribute("stats", stats);

        // ─── Recent bookings table ───────────────────────────────────────────
        model.addAttribute("recentBookings", service.getRecentBookings());

        // ─── Chart data (serialised to JSON for Chart.js) ────────────────────
        List<Map<String, Object>> trend = service.getBookingTrend();
        List<String> trendLabels = new ArrayList<>();
        List<Long>   trendData   = new ArrayList<>();
        for (Map<String, Object> row : trend) {
            trendLabels.add(row.get("day").toString());
            trendData.add(((Number) row.get("total")).longValue());
        }
        model.addAttribute("trendLabels", objectMapper.writeValueAsString(trendLabels));
        model.addAttribute("trendData",   objectMapper.writeValueAsString(trendData));

        // Revenue trend
        List<Map<String, Object>> revTrend = service.getRevenueTrend();
        List<String> revLabels = new ArrayList<>();
        List<Double> revData   = new ArrayList<>();
        for (Map<String, Object> row : revTrend) {
            revLabels.add(row.get("day").toString());
            revData.add(((Number) row.get("revenue")).doubleValue());
        }
        model.addAttribute("revLabels", objectMapper.writeValueAsString(revLabels));
        model.addAttribute("revData",   objectMapper.writeValueAsString(revData));

        // Status distribution for pie chart
        Map<String, Long> statusDist = service.getBookingStatusDistribution();
        model.addAttribute("statusDist", objectMapper.writeValueAsString(statusDist));

        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("activePage", "dashboard");
        return "dashboard";
    }
}
