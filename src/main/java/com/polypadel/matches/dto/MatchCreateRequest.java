package com.polypadel.matches.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.UUID;

public record MatchCreateRequest(
    @NotNull UUID evenementId,
    @NotNull UUID equipe1Id,
    @NotNull UUID equipe2Id,
    @NotNull @Min(1) Integer piste,
    @NotNull LocalTime startTime
) {}
