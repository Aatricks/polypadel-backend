package com.polypadel.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record ProfileUpdateRequest(
    @Size(min = 2, max = 50) String firstName,
    @Size(min = 2, max = 50) String lastName,
    LocalDate birthDate,
    @Email String email
) {}
