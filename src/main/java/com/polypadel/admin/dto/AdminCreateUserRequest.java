package com.polypadel.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AdminCreateUserRequest(
    @Email @NotBlank String email,
    @NotBlank String role
) {}
