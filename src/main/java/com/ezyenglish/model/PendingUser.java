package com.ezyenglish.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;

@Document(collection = "pending_users")
public class PendingUser {

    @Id
    private String id;

    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private Set<String> roles;

    private String otp;
    private LocalDateTime otpExpiryTime;

    public PendingUser() {
    }

    public PendingUser(String username, String email, String password,
                       String firstName, String lastName, String phone,
                       Set<String> roles, String otp, LocalDateTime otpExpiryTime) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.roles = roles;
        this.otp = otp;
        this.otpExpiryTime = otpExpiryTime;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }

    public LocalDateTime getOtpExpiryTime() { return otpExpiryTime; }
    public void setOtpExpiryTime(LocalDateTime otpExpiryTime) { this.otpExpiryTime = otpExpiryTime; }
}
