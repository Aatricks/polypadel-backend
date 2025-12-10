package com.polypadel.dto;

import jakarta.validation.constraints.*;

public record MatchUpdateRequest(
    String status,
    @Pattern(regexp = "^(\\d+-\\d+)(,\\s*\\d+-\\d+){1,2}$", message = "Format score invalide") String scoreTeam1,
    @Pattern(regexp = "^(\\d+-\\d+)(,\\s*\\d+-\\d+){1,2}$", message = "Format score invalide") String scoreTeam2
) {}
