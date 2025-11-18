package com.polypadel.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AdminCreateUserRequest {
    @Email
    @NotBlank
    public String email;
    @NotBlank
    public String role; // ADMIN or JOUEUR
}
