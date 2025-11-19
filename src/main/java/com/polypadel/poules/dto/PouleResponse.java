package com.polypadel.poules.dto;

import java.util.UUID;

public record PouleResponse(
    UUID id,
    String nom,
    int teamCount
) {}
