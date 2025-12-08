package com.polypadel.matches.dto;

import com.polypadel.domain.enums.MatchStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * DTO utilisé pour modifier un match existant (Score, Statut, Piste).
 */
public record MatchUpdateRequest(
    
    // Validation du format des scores (ex: "6-4" ou "6-4, 6-3")
    @Pattern(regexp = "^\\d+-\\d+(,\\s*\\d+-\\d+){1,2}$", message = "Format invalide (ex: 6-4, 6-3)")
    String scoreTeam1,

    @Pattern(regexp = "^\\d+-\\d+(,\\s*\\d+-\\d+){1,2}$", message = "Format invalide (ex: 6-4, 6-3)")
    String scoreTeam2,

    MatchStatus status, // "status" au lieu de "statut" pour l'API anglaise standard

    @Min(1) @Max(10)
    Integer courtNumber // Nouveau champ indispensable
) {}