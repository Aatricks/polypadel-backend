package com.polypadel.equipes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TeamCreateRequest(
    @NotBlank String entreprise,
    @NotNull UUID joueur1Id,
    @NotNull UUID joueur2Id,
    UUID pouleId
) {}
