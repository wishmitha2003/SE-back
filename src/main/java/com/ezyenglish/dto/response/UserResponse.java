package com.ezyenglish.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * User response DTO — returned for user profile queries.
 * Does NOT include the password field.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private List<String> roles;
    private Instant createdAt;

    // Student profile fields
    private String gradeLevel;
    private List<String> enrolledCourses;
    private double overallProgress;
    private int totalPoints;

    // Teacher profile fields
    private String subject;
    private String qualification;
    private List<String> taughtCourses;
    private String bio;

    // Parent profile fields
    private List<String> childStudentIds;
    private String relationship;
}
