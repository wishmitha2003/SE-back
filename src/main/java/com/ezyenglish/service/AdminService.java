package com.ezyenglish.service;

import com.ezyenglish.dto.response.UserResponse;
import com.ezyenglish.model.AdminProfile;
import com.ezyenglish.repository.AdminProfileRepository;
import com.ezyenglish.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for admin-specific operations (e.g., managing linked children).
 */
@Service
public class AdminService {

    @Autowired
    private AdminProfileRepository adminProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    /**
     * Get all children linked to an admin.
     */
    public List<UserResponse> getChildren(String adminUserId) {
        AdminProfile profile = adminProfileRepository.findByUserId(adminUserId)
                .orElseGet(() -> adminProfileRepository.save(new AdminProfile(adminUserId)));

        return profile.getChildStudentIds().stream()
                .map(childId -> {
                    try {
                        return userService.getUserById(childId);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Link a child student to an admin.
     */
    public AdminProfile addChild(String adminUserId, String childStudentId) {
        AdminProfile profile = adminProfileRepository.findByUserId(adminUserId)
                .orElseGet(() -> adminProfileRepository.save(new AdminProfile(adminUserId)));

        // Verify the child exists
        userRepository.findById(childStudentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + childStudentId));

        if (!profile.getChildStudentIds().contains(childStudentId)) {
            profile.getChildStudentIds().add(childStudentId);
            adminProfileRepository.save(profile);
        }

        return profile;
    }

    /**
     * Unlink a child student from an admin.
     */
    public AdminProfile removeChild(String adminUserId, String childStudentId) {
        AdminProfile profile = adminProfileRepository.findByUserId(adminUserId)
                .orElseGet(() -> adminProfileRepository.save(new AdminProfile(adminUserId)));

        profile.getChildStudentIds().remove(childStudentId);
        adminProfileRepository.save(profile);

        return profile;
    }

    /**
     * Get admin profile by user ID.
     */
    public AdminProfile getAdminProfile(String adminUserId) {
        return adminProfileRepository.findByUserId(adminUserId)
                .orElseGet(() -> adminProfileRepository.save(new AdminProfile(adminUserId)));
    }
}
