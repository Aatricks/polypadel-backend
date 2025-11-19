package com.polypadel.joueurs.dto;

import java.time.LocalDate;
import java.util.UUID;

public record PlayerResponse(
    UUID id,
    String numLicence,
    String nom,
    String prenom,
    LocalDate dateNaissance,
    String photoUrl,
    String entreprise
) {}
