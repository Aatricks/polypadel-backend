package com.polypadel.users.dto;

import java.time.LocalDate;
import java.util.UUID;

public class ProfileResponse {
    public UUID userId;
    public String email;
    public String role;

    public UUID playerId;
    public String nom;
    public String prenom;
    public LocalDate dateNaissance;
    public String photoUrl;
    public String entreprise;
}
