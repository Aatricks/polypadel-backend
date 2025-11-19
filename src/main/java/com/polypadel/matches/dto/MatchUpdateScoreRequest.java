package com.polypadel.matches.dto;

import com.polypadel.domain.enums.MatchStatus;

public record MatchUpdateScoreRequest(
    String score1,
    String score2,
    MatchStatus statut
) {}
