package com.polypadel.joueurs.dto;

import java.time.LocalDate;
import java.util.UUID;

public class PlayerResponse {
    public UUID id;
    public String numLicence;
    public String nom;
    public String prenom;
    public LocalDate dateNaissance;
    public String photoUrl;
    public String entreprise;
}
