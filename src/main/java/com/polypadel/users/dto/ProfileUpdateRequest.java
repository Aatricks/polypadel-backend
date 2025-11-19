package com.polypadel.users.dto;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProfileUpdateRequest(
    @Size(min = 2, max = 50) String nom,
    @Size(min = 2, max = 50) String prenom,
    @Past LocalDate dateNaissance,
    @Size(max = 1024) String photoUrl
) {}
