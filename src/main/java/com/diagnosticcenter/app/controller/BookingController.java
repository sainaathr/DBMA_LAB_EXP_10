package com.diagnosticcenter.app.controller;

import com.diagnosticcenter.app.model.Booking;
import com.diagnosticcenter.app.service.DiagnosticService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    private static final int PAGE_SIZE = 10;
    private final DiagnosticService service;

    public BookingController(DiagnosticService service) { this.service = service; }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String search,
                       @RequestParam(defaultValue = "") String status,
                       @RequestParam(defaultValue = "1") int page,
                       Model model) {
        int total = service.countBookings(search, status);
        int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
        model.addAttribute("bookings", service.getBookings(search, status, page, PAGE_SIZE));
        model.addAttribute("search", search);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("total", total);
        model.addAttribute("pageTitle", "Bookings");
        model.addAttribute("activePage", "bookings");
        return "bookings/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        Booking booking = new Booking();
        booking.setStatus("Pending");
        model.addAttribute("booking", booking);
        model.addAttribute("patients", service.getPatients(null, 1, 500));
        model.addAttribute("centers",  service.getCentersForDropdown());
        model.addAttribute("doctors",  service.getDoctors(null, null));
        model.addAttribute("tests",    service.getTests(null));
        model.addAttribute("pageTitle", "New Booking");
        model.addAttribute("activePage", "bookings");
        return "bookings/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute Booking booking, BindingResult result,
                         Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("patients", service.getPatients(null, 1, 500));
            model.addAttribute("centers",  service.getCentersForDropdown());
            model.addAttribute("doctors",  service.getDoctors(null, null));
            model.addAttribute("tests",    service.getTests(null));
            model.addAttribute("pageTitle", "New Booking");
            model.addAttribute("activePage", "bookings");
            return "bookings/form";
        }
        try {
            int id = service.createBooking(booking);
            ra.addFlashAttribute("successMessage", "Booking #" + id + " created successfully.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/bookings";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable int id, Model model) {
        Booking booking = service.getBooking(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + id));
        model.addAttribute("booking", booking);
        model.addAttribute("existingBill", service.getBillByBookingId(id).orElse(null));
        model.addAttribute("pageTitle", "Booking #" + id);
        model.addAttribute("activePage", "bookings");
        return "bookings/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable int id, @RequestParam String status,
                               RedirectAttributes ra) {
        List<String> validStatuses = List.of("Pending", "Confirmed", "Completed", "Cancelled");
        if (!validStatuses.contains(status)) {
            ra.addFlashAttribute("errorMessage", "Invalid status.");
            return "redirect:/bookings/" + id;
        }
        service.updateBookingStatus(id, status);
        ra.addFlashAttribute("successMessage", "Booking status updated to " + status);
        return "redirect:/bookings/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes ra) {
        service.deleteBooking(id);
        ra.addFlashAttribute("successMessage", "Booking deleted.");
        return "redirect:/bookings";
    }
}
