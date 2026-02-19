package com.ezyenglish.controller;

import com.ezyenglish.model.StudentProfile;
import com.ezyenglish.model.TeacherProfile;
import com.ezyenglish.security.service.UserDetailsImpl;
import com.ezyenglish.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Teacher-specific endpoints.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    /**
     * POST /api/teachers/{id}/courses — Assign a course to a teacher.
     */
    @PostMapping("/{id}/courses")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherProfile> assignCourse(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {

        String courseId = request.get("courseId");
        TeacherProfile profile = teacherService.assignCourse(id, courseId);
        return ResponseEntity.ok(profile);
    }

    /**
     * DELETE /api/teachers/{id}/courses/{courseId} — Remove a course from a teacher.
     */
    @DeleteMapping("/{id}/courses/{courseId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherProfile> removeCourse(
            @PathVariable String id,
            @PathVariable String courseId) {

        TeacherProfile profile = teacherService.removeCourse(id, courseId);
        return ResponseEntity.ok(profile);
    }

    /**
     * GET /api/teachers/profile — Get own teacher profile.
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherProfile> getOwnProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        TeacherProfile profile = teacherService.getTeacherProfile(userDetails.getId());
        return ResponseEntity.ok(profile);
    }

    /**
     * GET /api/teachers/{id}/students — Get students in teacher's courses.
     */
    @GetMapping("/{id}/students")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<StudentProfile>> getStudentsByCourse(
            @PathVariable String id,
            @RequestParam String courseId) {

        List<StudentProfile> students = teacherService.getStudentsByCourse(courseId);
        return ResponseEntity.ok(students);
    }
}
