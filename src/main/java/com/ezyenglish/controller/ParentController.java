package com.ezyenglish.controller;

import com.ezyenglish.dto.response.UserResponse;
import com.ezyenglish.model.ParentProfile;
import com.ezyenglish.security.service.UserDetailsImpl;
import com.ezyenglish.service.ParentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Parent-specific endpoints.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/parents")
public class ParentController {

    @Autowired
    private ParentService parentService;

    /**
     * GET /api/parents/{id}/children — Get linked students for a parent.
     */
    @GetMapping("/{id}/children")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<UserResponse>> getChildren(@PathVariable String id) {
        List<UserResponse> children = parentService.getChildren(id);
        return ResponseEntity.ok(children);
    }

    /**
     * POST /api/parents/{id}/children — Link a child student to parent.
     */
    @PostMapping("/{id}/children")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ParentProfile> addChild(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {

        String childStudentId = request.get("childStudentId");
        ParentProfile profile = parentService.addChild(id, childStudentId);
        return ResponseEntity.ok(profile);
    }

    /**
     * DELETE /api/parents/{id}/children/{childId} — Unlink a child student.
     */
    @DeleteMapping("/{id}/children/{childId}")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ParentProfile> removeChild(
            @PathVariable String id,
            @PathVariable String childId) {

        ParentProfile profile = parentService.removeChild(id, childId);
        return ResponseEntity.ok(profile);
    }

    /**
     * GET /api/parents/profile — Get own parent profile.
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ParentProfile> getOwnProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        ParentProfile profile = parentService.getParentProfile(userDetails.getId());
        return ResponseEntity.ok(profile);
    }
}
