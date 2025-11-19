package com.polypadel.users.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ProfileResponse(
    UUID userId,
    String email,
    String role,
    UUID playerId,
    String nom,
    String prenom,
    LocalDate dateNaissance,
    String photoUrl,
    String entreprise
) {}
