package com.polypadel.joueurs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class PlayerCreateRequest {
    @NotBlank
    public String numLicence;
    @Size(min = 2, max = 50)
    public String nom;
    @Size(min = 2, max = 50)
    public String prenom;
    @Past
    public LocalDate dateNaissance;
    @Size(max = 1024)
    public String photoUrl;
    @NotBlank
    public String entreprise;
}
