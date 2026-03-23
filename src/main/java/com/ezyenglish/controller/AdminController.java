package com.ezyenglish.controller;

import com.ezyenglish.dto.response.UserResponse;
import com.ezyenglish.model.AdminProfile;
import com.ezyenglish.security.service.UserDetailsImpl;
import com.ezyenglish.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin-specific endpoints.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admins")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * GET /api/admins/{id}/children — Get linked students for an admin.
     */
    @GetMapping("/{id}/children")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getChildren(@PathVariable("id") String id) {
        List<UserResponse> children = adminService.getChildren(id);
        return ResponseEntity.ok(children);
    }

    /**
     * POST /api/admins/{id}/children — Link a child student to admin.
     */
    @PostMapping("/{id}/children")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminProfile> addChild(
            @PathVariable("id") String id,
            @RequestBody Map<String, String> request) {

        String childStudentId = request.get("childStudentId");
        AdminProfile profile = adminService.addChild(id, childStudentId);
        return ResponseEntity.ok(profile);
    }

    /**
     * DELETE /api/admins/{id}/children/{childId} — Unlink a child student.
     */
    @DeleteMapping("/{id}/children/{childId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminProfile> removeChild(
            @PathVariable("id") String id,
            @PathVariable("childId") String childId) {

        AdminProfile profile = adminService.removeChild(id, childId);
        return ResponseEntity.ok(profile);
    }

    /**
     * GET /api/admins/profile — Get own admin profile.
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminProfile> getOwnProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        AdminProfile profile = adminService.getAdminProfile(userDetails.getId());
        return ResponseEntity.ok(profile);
    }
}
