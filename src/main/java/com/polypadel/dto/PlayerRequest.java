package com.polypadel.dto;

import jakarta.validation.constraints.*;

public record PlayerRequest(
    @NotBlank @Size(min = 2, max = 50) String firstName,
    @NotBlank @Size(min = 2, max = 50) String lastName,
    @NotBlank @Size(min = 2, max = 100) String company,
    @NotBlank @Pattern(regexp = "^L\\d{6}$", message = "Format LXXXXXX requis") String licenseNumber,
    @NotBlank @Email String email
) {}
