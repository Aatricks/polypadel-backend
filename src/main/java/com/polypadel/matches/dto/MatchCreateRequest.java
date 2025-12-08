package com.polypadel.matches.dto;

import jakarta.validation.constraints.Max; 
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime; 
import java.util.UUID;

public record MatchCreateRequest(
    @NotNull 
    UUID evenementId,

    @NotNull 
    UUID equipe1Id,

    @NotNull 
    UUID equipe2Id,

    @NotNull 
    @Min(1) 
    @Max(10) 
    Integer piste,

    @NotNull 
    LocalDateTime startTime 
) {}