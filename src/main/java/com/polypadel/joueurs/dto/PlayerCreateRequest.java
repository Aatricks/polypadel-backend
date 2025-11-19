package com.polypadel.joueurs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PlayerCreateRequest(
    @NotBlank String numLicence,
    @Size(min = 2, max = 50) String nom,
    @Size(min = 2, max = 50) String prenom,
    @Past LocalDate dateNaissance,
    @Size(max = 1024) String photoUrl,
    @NotBlank String entreprise
) {}
