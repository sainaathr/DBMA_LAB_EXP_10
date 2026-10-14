package com.diagnosticcenter.app.controller;

import com.diagnosticcenter.app.model.Patient;
import com.diagnosticcenter.app.service.DiagnosticService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Patient CRUD controller. */
@Controller
@RequestMapping("/patients")
public class PatientController {

    private static final int PAGE_SIZE = 10;
    private final DiagnosticService service;

    public PatientController(DiagnosticService service) { this.service = service; }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String search,
                       @RequestParam(defaultValue = "1") int page, Model model) {
        int total = service.countPatients(search);
        int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
        List<Patient> patients = service.getPatients(search, page, PAGE_SIZE);
        model.addAttribute("patients", patients);
        model.addAttribute("search", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("total", total);
        model.addAttribute("pageTitle", "Patients");
        model.addAttribute("activePage", "patients");
        return "patients/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("patient", new Patient());
        model.addAttribute("pageTitle", "Add Patient");
        model.addAttribute("activePage", "patients");
        return "patients/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute Patient patient, BindingResult result,
                         Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Add Patient");
            model.addAttribute("activePage", "patients");
            return "patients/form";
        }
        service.createPatient(patient);
        ra.addFlashAttribute("successMessage", "Patient added successfully.");
        return "redirect:/patients";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        Patient patient = service.getPatient(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + id));
        model.addAttribute("patient", patient);
        model.addAttribute("pageTitle", "Edit Patient");
        model.addAttribute("activePage", "patients");
        return "patients/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable int id, @Valid @ModelAttribute Patient patient,
                         BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            patient.setPatientId(id);
            model.addAttribute("pageTitle", "Edit Patient");
            model.addAttribute("activePage", "patients");
            return "patients/form";
        }
        patient.setPatientId(id);
        service.updatePatient(patient);
        ra.addFlashAttribute("successMessage", "Patient updated successfully.");
        return "redirect:/patients";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable int id, Model model) {
        Patient patient = service.getPatient(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + id));
        model.addAttribute("patient", patient);
        model.addAttribute("pageTitle", "Patient Details");
        model.addAttribute("activePage", "patients");
        return "patients/detail";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes ra) {
        service.deletePatient(id);
        ra.addFlashAttribute("successMessage", "Patient deleted.");
        return "redirect:/patients";
    }
}
