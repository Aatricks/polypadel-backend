package com.polypadel.dto;

import jakarta.validation.constraints.*;

public record MatchCreateRequest(
    @NotNull Long eventId,
    @NotNull Long team1Id,
    @NotNull Long team2Id,
    @NotNull @Min(1) Integer courtNumber
) {}
