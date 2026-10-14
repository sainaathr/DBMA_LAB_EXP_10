package com.diagnosticcenter.app.controller;

import com.diagnosticcenter.app.model.Test;
import com.diagnosticcenter.app.service.DiagnosticService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tests")
public class TestController {

    private final DiagnosticService service;

    public TestController(DiagnosticService service) { this.service = service; }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String search, Model model) {
        model.addAttribute("tests", service.getTests(search));
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "Medical Tests");
        model.addAttribute("activePage", "tests");
        return "tests/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("test", new Test());
        model.addAttribute("pageTitle", "Add Test");
        model.addAttribute("activePage", "tests");
        return "tests/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute Test test, BindingResult result,
                         Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Add Test");
            model.addAttribute("activePage", "tests");
            return "tests/form";
        }
        service.createTest(test);
        ra.addFlashAttribute("successMessage", "Test added successfully.");
        return "redirect:/tests";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        Test test = service.getTest(id)
                .orElseThrow(() -> new IllegalArgumentException("Test not found: " + id));
        model.addAttribute("test", test);
        model.addAttribute("pageTitle", "Edit Test");
        model.addAttribute("activePage", "tests");
        return "tests/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable int id, @Valid @ModelAttribute Test test,
                         BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Test");
            model.addAttribute("activePage", "tests");
            return "tests/form";
        }
        test.setTestId(id);
        service.updateTest(test);
        ra.addFlashAttribute("successMessage", "Test updated successfully.");
        return "redirect:/tests";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes ra) {
        service.deleteTest(id);
        ra.addFlashAttribute("successMessage", "Test deleted.");
        return "redirect:/tests";
    }
}
