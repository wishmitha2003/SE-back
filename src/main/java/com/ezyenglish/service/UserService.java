package com.ezyenglish.service;

import com.ezyenglish.dto.request.UserUpdateRequest;
import com.ezyenglish.dto.response.UserResponse;
import com.ezyenglish.model.*;
import com.ezyenglish.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service handling user CRUD operations and profile management.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private ParentProfileRepository parentProfileRepository;

    /**
     * Get a full user response (user + role-specific profile) by user ID.
     */
    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return buildUserResponse(user);
    }

    /**
     * Get a full user response by username.
     */
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        return buildUserResponse(user);
    }

    /**
     * Get all users (for admin purposes).
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::buildUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get users by role.
     */
    public List<UserResponse> getUsersByRole(ERole role) {
        return userRepository.findByRolesName(role).stream()
                .map(this::buildUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update user profile (common + role-specific fields).
     */
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Update common fields
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPhone() != null) user.setPhone(request.getPhone());

        userRepository.save(user);

        // Update role-specific profiles
        updateRoleSpecificProfile(user, request);

        return buildUserResponse(user);
    }

    /**
     * Delete a user and their associated profile.
     */
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Delete associated profiles
        studentProfileRepository.findByUserId(userId).ifPresent(p ->
                studentProfileRepository.delete(p));
        teacherProfileRepository.findByUserId(userId).ifPresent(p ->
                teacherProfileRepository.delete(p));
        parentProfileRepository.findByUserId(userId).ifPresent(p ->
                parentProfileRepository.delete(p));

        userRepository.delete(user);
    }

    /**
     * Build a UserResponse from a User entity, enriching with profile data.
     */
    private UserResponse buildUserResponse(User user) {
        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPhone(user.getPhone());
        response.setCreatedAt(user.getCreatedAt());
        response.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()));

        // Enrich with Student profile
        studentProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
            response.setGradeLevel(profile.getGradeLevel());
            response.setEnrolledCourses(profile.getEnrolledCourses());
            response.setOverallProgress(profile.getOverallProgress());
            response.setTotalPoints(profile.getTotalPoints());
        });

        // Enrich with Teacher profile
        teacherProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
            response.setSubject(profile.getSubject());
            response.setQualification(profile.getQualification());
            response.setTaughtCourses(profile.getTaughtCourses());
            response.setBio(profile.getBio());
        });

        // Enrich with Parent profile
        parentProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
            response.setChildStudentIds(profile.getChildStudentIds());
            response.setRelationship(profile.getRelationship());
        });

        return response;
    }

    /**
     * Update the appropriate role-specific profile based on user's roles.
     */
    private void updateRoleSpecificProfile(User user, UserUpdateRequest request) {
        boolean isStudent = user.getRoles().stream()
                .anyMatch(r -> r.getName() == ERole.ROLE_STUDENT);
        boolean isTeacher = user.getRoles().stream()
                .anyMatch(r -> r.getName() == ERole.ROLE_TEACHER);
        boolean isParent = user.getRoles().stream()
                .anyMatch(r -> r.getName() == ERole.ROLE_PARENT);

        if (isStudent && (request.getGradeLevel() != null)) {
            StudentProfile profile = studentProfileRepository.findByUserId(user.getId())
                    .orElse(new StudentProfile(user.getId()));
            if (request.getGradeLevel() != null) profile.setGradeLevel(request.getGradeLevel());
            studentProfileRepository.save(profile);
        }

        if (isTeacher) {
            TeacherProfile profile = teacherProfileRepository.findByUserId(user.getId())
                    .orElse(new TeacherProfile(user.getId()));
            if (request.getSubject() != null) profile.setSubject(request.getSubject());
            if (request.getQualification() != null) profile.setQualification(request.getQualification());
            if (request.getBio() != null) profile.setBio(request.getBio());
            teacherProfileRepository.save(profile);
        }

        if (isParent && (request.getRelationship() != null)) {
            ParentProfile profile = parentProfileRepository.findByUserId(user.getId())
                    .orElse(new ParentProfile(user.getId()));
            if (request.getRelationship() != null) profile.setRelationship(request.getRelationship());
            parentProfileRepository.save(profile);
        }
    }
}
