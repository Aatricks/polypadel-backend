package com.polypadel.users.dto;

import jakarta.validation.constraints.NotBlank;

public class PasswordUpdateRequest {
    @NotBlank
    public String currentPassword;
    @NotBlank
    public String newPassword;
}
