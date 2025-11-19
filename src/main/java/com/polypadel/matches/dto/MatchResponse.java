package com.polypadel.matches.dto;

import com.polypadel.domain.enums.MatchStatus;

import java.time.LocalTime;
import java.util.UUID;

public record MatchResponse(
    UUID id,
    UUID evenementId,
    UUID equipe1Id,
    UUID equipe2Id,
    Integer piste,
    LocalTime startTime,
    MatchStatus statut,
    String score1,
    String score2
) {}
