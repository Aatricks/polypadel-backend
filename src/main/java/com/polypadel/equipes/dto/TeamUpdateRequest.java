package com.polypadel.equipes.dto;

import java.util.UUID;

public record TeamUpdateRequest(
    String entreprise,
    UUID joueur1Id,
    UUID joueur2Id,
    UUID pouleId
) {}
