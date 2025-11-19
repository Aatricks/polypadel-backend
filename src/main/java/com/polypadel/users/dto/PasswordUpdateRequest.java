package com.polypadel.users.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordUpdateRequest(
    @NotBlank String currentPassword,
    @NotBlank String newPassword
) {}
