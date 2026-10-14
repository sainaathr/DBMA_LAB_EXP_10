package com.diagnosticcenter.app.controller;

import com.diagnosticcenter.app.model.Center;
import com.diagnosticcenter.app.service.DiagnosticService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/centers")
public class CenterController {

    private final DiagnosticService service;

    public CenterController(DiagnosticService service) { this.service = service; }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String search, Model model) {
        model.addAttribute("centers", service.getCenters(search));
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "Diagnostic Centers");
        model.addAttribute("activePage", "centers");
        return "centers/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("center", new Center());
        model.addAttribute("pageTitle", "Add Center");
        model.addAttribute("activePage", "centers");
        return "centers/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute Center center, BindingResult result,
                         Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Add Center");
            model.addAttribute("activePage", "centers");
            return "centers/form";
        }
        service.createCenter(center);
        ra.addFlashAttribute("successMessage", "Center added successfully.");
        return "redirect:/centers";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        Center center = service.getCenter(id)
                .orElseThrow(() -> new IllegalArgumentException("Center not found: " + id));
        model.addAttribute("center", center);
        model.addAttribute("pageTitle", "Edit Center");
        model.addAttribute("activePage", "centers");
        return "centers/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable int id, @Valid @ModelAttribute Center center,
                         BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Center");
            model.addAttribute("activePage", "centers");
            return "centers/form";
        }
        center.setCenterId(id);
        service.updateCenter(center);
        ra.addFlashAttribute("successMessage", "Center updated successfully.");
        return "redirect:/centers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes ra) {
        service.deleteCenter(id);
        ra.addFlashAttribute("successMessage", "Center deleted.");
        return "redirect:/centers";
    }
}
