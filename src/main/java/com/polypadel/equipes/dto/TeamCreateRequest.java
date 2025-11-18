package com.polypadel.equipes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class TeamCreateRequest {
    @NotBlank
    public String entreprise;
    @NotNull
    public UUID joueur1Id;
    @NotNull
    public UUID joueur2Id;
    public UUID pouleId; // optional
}
