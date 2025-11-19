package com.polypadel.joueurs.dto;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PlayerUpdateRequest(
    @Size(min = 2, max = 50) String nom,
    @Size(min = 2, max = 50) String prenom,
    @Past LocalDate dateNaissance,
    @Size(max = 1024) String photoUrl,
    @Size(min = 1, max = 128) String entreprise
) {}
