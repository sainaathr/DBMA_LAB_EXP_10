package com.diagnosticcenter.app.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Global exception handler — converts exceptions to user-friendly error pages.
 * Never exposes raw stack traces or database error messages to the browser.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Handle 404 — URL not found. */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(HttpServletRequest req, Model model) {
        model.addAttribute("errorMessage", "The page you requested was not found.");
        model.addAttribute("path", req.getRequestURI());
        return "error/404";
    }

    /**
     * Foreign key / unique constraint violations.
     * For example: deleting a center that still has doctors.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String dataIntegrityViolation(DataIntegrityViolationException ex,
                                         HttpServletRequest req, Model model) {
        log.warn("Data integrity violation at {}: {}", req.getRequestURI(), ex.getMessage());
        model.addAttribute("errorMessage",
            "This operation was not allowed because other records depend on this data. " +
            "Please remove related records first.");
        return "error/500";
    }

    /** Catch-all for unexpected errors. */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String genericError(Exception ex, HttpServletRequest req, Model model) {
        log.error("Unexpected error at {}", req.getRequestURI(), ex);
        model.addAttribute("errorMessage",
            "An unexpected error occurred. Please try again or contact support.");
        return "error/500";
    }
}
