package com.polypadel.dto;

import jakarta.validation.constraints.*;

public record PasswordChangeRequest(
    @NotBlank String currentPassword,
    @NotBlank @Size(min = 12) String newPassword,
    @NotBlank String confirmPassword
) {}
