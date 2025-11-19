package com.polypadel.equipes.dto;

import java.util.UUID;

public record TeamResponse(
    UUID id,
    String entreprise,
    UUID pouleId,
    UUID joueur1Id,
    UUID joueur2Id
) {}
