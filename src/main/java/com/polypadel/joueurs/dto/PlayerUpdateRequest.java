package com.polypadel.joueurs.dto;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class PlayerUpdateRequest {
    @Size(min = 2, max = 50)
    public String nom;
    @Size(min = 2, max = 50)
    public String prenom;
    @Past
    public LocalDate dateNaissance;
    @Size(max = 1024)
    public String photoUrl;
    @Size(min = 1, max = 128)
    public String entreprise;
}
