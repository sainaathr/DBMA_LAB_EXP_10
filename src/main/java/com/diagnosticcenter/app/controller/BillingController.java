package com.diagnosticcenter.app.controller;

import com.diagnosticcenter.app.model.Bill;
import com.diagnosticcenter.app.service.DiagnosticService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/billing")
public class BillingController {

    private static final int PAGE_SIZE = 10;
    private final DiagnosticService service;

    public BillingController(DiagnosticService service) { this.service = service; }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String status,
                       @RequestParam(defaultValue = "1") int page,
                       Model model) {
        int total = service.countBills(status);
        int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
        model.addAttribute("bills", service.getBills(status, page, PAGE_SIZE));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("total", total);
        model.addAttribute("pageTitle", "Billing");
        model.addAttribute("activePage", "billing");
        return "billing/list";
    }

    /** Generate bill for a booking (server-side total calculation). */
    @PostMapping("/generate")
    public String generate(@RequestParam int bookingId,
                           @RequestParam(defaultValue = "0") BigDecimal discountAmount,
                           RedirectAttributes ra) {
        try {
            int billId = service.generateBill(bookingId, discountAmount);
            ra.addFlashAttribute("successMessage", "Bill #" + billId + " generated.");
            return "redirect:/billing/" + billId;
        } catch (IllegalStateException | IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/bookings/" + bookingId;
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable int id, Model model) {
        Bill bill = service.getBill(id)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found: " + id));
        model.addAttribute("bill", bill);
        // Load booking + tests for printable receipt
        model.addAttribute("booking", service.getBooking(bill.getBookingId()).orElse(null));
        model.addAttribute("pageTitle", "Bill #" + id);
        model.addAttribute("activePage", "billing");
        return "billing/detail";
    }

    /** Record a payment (validates on server — no client-submitted totals). */
    @PostMapping("/{id}/pay")
    public String recordPayment(@PathVariable int id,
                                @RequestParam BigDecimal paymentAmount,
                                @RequestParam String paymentMethod,
                                RedirectAttributes ra) {
        try {
            service.recordPayment(id, paymentAmount, paymentMethod);
            ra.addFlashAttribute("successMessage", "Payment of ₹" + paymentAmount + " recorded.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/billing/" + id;
    }
}
