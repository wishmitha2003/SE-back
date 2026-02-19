package com.ezyenglish.controller;

import com.ezyenglish.dto.request.UserUpdateRequest;
import com.ezyenglish.dto.response.MessageResponse;
import com.ezyenglish.dto.response.UserResponse;
import com.ezyenglish.security.service.UserDetailsImpl;
import com.ezyenglish.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User management controller — CRUD operations with role-based access.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * GET /api/users/profile — Get the authenticated user's own profile.
     */
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getOwnProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        UserResponse response = userService.getUserById(userDetails.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/users/{id} — Get any user by ID (Teachers & Parents only).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('PARENT')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/users — Get all users (Teachers & Parents).
     */
    @GetMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('PARENT')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * PUT /api/users/{id} — Update a user's profile.
     * Users can update their own profile; teachers/parents can update others.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UserUpdateRequest request) {

        // Verify the user is updating their own profile or has elevated role
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        boolean isOwnProfile = userDetails.getId().equals(id);
        boolean hasElevatedRole = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER") ||
                              a.getAuthority().equals("ROLE_PARENT"));

        if (!isOwnProfile && !hasElevatedRole) {
            return ResponseEntity.status(403).build();
        }

        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/users/{id} — Delete a user (Teachers only — acting as admin).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully!"));
    }
}
