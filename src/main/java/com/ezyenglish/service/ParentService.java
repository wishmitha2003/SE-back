package com.ezyenglish.service;

import com.ezyenglish.dto.response.UserResponse;
import com.ezyenglish.model.ParentProfile;
import com.ezyenglish.model.User;
import com.ezyenglish.repository.ParentProfileRepository;
import com.ezyenglish.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for parent-specific operations (e.g., managing linked children).
 */
@Service
public class ParentService {

    @Autowired
    private ParentProfileRepository parentProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    /**
     * Get all children linked to a parent.
     */
    public List<UserResponse> getChildren(String parentUserId) {
        ParentProfile profile = parentProfileRepository.findByUserId(parentUserId)
                .orElseThrow(() -> new RuntimeException("Parent profile not found for userId: " + parentUserId));

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
     * Link a child student to a parent.
     */
    public ParentProfile addChild(String parentUserId, String childStudentId) {
        ParentProfile profile = parentProfileRepository.findByUserId(parentUserId)
                .orElseThrow(() -> new RuntimeException("Parent profile not found for userId: " + parentUserId));

        // Verify the child exists
        userRepository.findById(childStudentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + childStudentId));

        if (!profile.getChildStudentIds().contains(childStudentId)) {
            profile.getChildStudentIds().add(childStudentId);
            parentProfileRepository.save(profile);
        }

        return profile;
    }

    /**
     * Unlink a child student from a parent.
     */
    public ParentProfile removeChild(String parentUserId, String childStudentId) {
        ParentProfile profile = parentProfileRepository.findByUserId(parentUserId)
                .orElseThrow(() -> new RuntimeException("Parent profile not found for userId: " + parentUserId));

        profile.getChildStudentIds().remove(childStudentId);
        parentProfileRepository.save(profile);

        return profile;
    }

    /**
     * Get parent profile by user ID.
     */
    public ParentProfile getParentProfile(String parentUserId) {
        return parentProfileRepository.findByUserId(parentUserId)
                .orElseThrow(() -> new RuntimeException("Parent profile not found for userId: " + parentUserId));
    }
}
