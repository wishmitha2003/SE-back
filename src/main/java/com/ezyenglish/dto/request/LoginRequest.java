package com.ezyenglish.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO for user login (signin).
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
