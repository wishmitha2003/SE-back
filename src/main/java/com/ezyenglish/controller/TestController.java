package com.ezyenglish.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller for verifying role-based access control.
 * Public endpoint: /api/test/all
 * Protected endpoints require specific roles.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/all")
    public String allAccess() {
        return "Ezy English API — Public Content";
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public String studentAccess() {
        return "Student Dashboard Content";
    }

    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public String teacherAccess() {
        return "Teacher Dashboard Content";
    }

    @GetMapping("/parent")
    @PreAuthorize("hasRole('PARENT')")
    public String parentAccess() {
        return "Parent Dashboard Content";
    }
}
