package com.polypadel.dto;

import jakarta.validation.constraints.NotNull;

public record TeamRequest(
    @NotNull String company,
    @NotNull Long player1Id,
    @NotNull Long player2Id,
    Long poolId
) {}
