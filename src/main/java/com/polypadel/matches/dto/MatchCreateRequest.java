package com.polypadel.matches.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.UUID;

public class MatchCreateRequest {
    @NotNull
    public UUID evenementId;
    @NotNull
    public UUID equipe1Id;
    @NotNull
    public UUID equipe2Id;
    @NotNull
    @Min(1)
    public Integer piste;
    @NotNull
    public LocalTime startTime;
}
