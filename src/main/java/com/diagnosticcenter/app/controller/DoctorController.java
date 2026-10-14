package com.diagnosticcenter.app.controller;

import com.diagnosticcenter.app.model.Doctor;
import com.diagnosticcenter.app.service.DiagnosticService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/doctors")
public class DoctorController {

    private final DiagnosticService service;

    public DoctorController(DiagnosticService service) { this.service = service; }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String search,
                       @RequestParam(defaultValue = "") String specialization,
                       Model model) {
        model.addAttribute("doctors", service.getDoctors(search, specialization));
        model.addAttribute("specializations", service.getSpecializations());
        model.addAttribute("centers", service.getCentersForDropdown());
        model.addAttribute("search", search);
        model.addAttribute("selectedSpec", specialization);
        model.addAttribute("pageTitle", "Doctors");
        model.addAttribute("activePage", "doctors");
        return "doctors/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("doctor", new Doctor());
        model.addAttribute("centers", service.getCentersForDropdown());
        model.addAttribute("pageTitle", "Add Doctor");
        model.addAttribute("activePage", "doctors");
        return "doctors/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute Doctor doctor, BindingResult result,
                         Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("centers", service.getCentersForDropdown());
            model.addAttribute("pageTitle", "Add Doctor");
            model.addAttribute("activePage", "doctors");
            return "doctors/form";
        }
        service.createDoctor(doctor);
        ra.addFlashAttribute("successMessage", "Doctor added successfully.");
        return "redirect:/doctors";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        Doctor doctor = service.getDoctor(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + id));
        model.addAttribute("doctor", doctor);
        model.addAttribute("centers", service.getCentersForDropdown());
        model.addAttribute("pageTitle", "Edit Doctor");
        model.addAttribute("activePage", "doctors");
        return "doctors/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable int id, @Valid @ModelAttribute Doctor doctor,
                         BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("centers", service.getCentersForDropdown());
            model.addAttribute("pageTitle", "Edit Doctor");
            model.addAttribute("activePage", "doctors");
            return "doctors/form";
        }
        doctor.setDoctorId(id);
        service.updateDoctor(doctor);
        ra.addFlashAttribute("successMessage", "Doctor updated successfully.");
        return "redirect:/doctors";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes ra) {
        service.deleteDoctor(id);
        ra.addFlashAttribute("successMessage", "Doctor deleted.");
        return "redirect:/doctors";
    }
}
