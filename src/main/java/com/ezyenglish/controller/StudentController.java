package com.ezyenglish.controller;

import com.ezyenglish.dto.response.UserResponse;
import com.ezyenglish.model.ERole;
import com.ezyenglish.model.StudentProfile;
import com.ezyenglish.security.service.UserDetailsImpl;
import com.ezyenglish.service.StudentService;
import com.ezyenglish.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Student-specific endpoints.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserService userService;

    /**
     * GET /api/students — List all students (Teachers & Parents).
     */
    @GetMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('PARENT')")
    public ResponseEntity<List<UserResponse>> getAllStudents() {
        List<UserResponse> students = userService.getUsersByRole(ERole.ROLE_STUDENT);
        return ResponseEntity.ok(students);
    }

    /**
     * POST /api/students/enroll — Enroll self in a course (Students only).
     */
    @PostMapping("/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentProfile> enrollInCourse(@RequestBody Map<String, String> request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        String courseId = request.get("courseId");

        StudentProfile profile = studentService.enrollInCourse(userDetails.getId(), courseId);
        return ResponseEntity.ok(profile);
    }

    /**
     * POST /api/students/unenroll — Unenroll self from a course (Students only).
     */
    @PostMapping("/unenroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentProfile> unenrollFromCourse(@RequestBody Map<String, String> request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        String courseId = request.get("courseId");

        StudentProfile profile = studentService.unenrollFromCourse(userDetails.getId(), courseId);
        return ResponseEntity.ok(profile);
    }

    /**
     * GET /api/students/profile — Get own student profile.
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentProfile> getOwnProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        StudentProfile profile = studentService.getStudentProfile(userDetails.getId());
        return ResponseEntity.ok(profile);
    }
}
