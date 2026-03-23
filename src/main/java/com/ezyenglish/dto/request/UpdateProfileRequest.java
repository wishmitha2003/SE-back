package com.ezyenglish.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String phone;

    private String address;
    private String city;
    private String postalCode;
    private String country;
    private String profileImageUrl;
}