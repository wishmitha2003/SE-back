package com.ezyenglish.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for updating user profile information.
 */
@Data
public class UserUpdateRequest {

    @Size(max = 20)
    private String username;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Size(max = 100)
    private String email;

    private String phone;
    private String address;
    private String city;
    private String postalCode;
    private String country;

    // Student-specific
    private String gradeLevel;

    // Teacher-specific
    private String subject;
    private String qualification;
    private String bio;

    // Parent-specific
    private String relationship;
}
