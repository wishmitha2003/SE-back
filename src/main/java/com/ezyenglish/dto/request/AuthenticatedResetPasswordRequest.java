package com.ezyenglish.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthenticatedResetPasswordRequest {
    @NotBlank
    private String otp;

    @NotBlank
    @Size(min = 6, max = 40)
    private String newPassword;

    // getters/setters
    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
