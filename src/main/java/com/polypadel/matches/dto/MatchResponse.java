package com.polypadel.matches.dto;

import com.polypadel.domain.enums.MatchStatus;

import java.time.LocalTime;
import java.util.UUID;

public class MatchResponse {
    public UUID id;
    public UUID evenementId;
    public UUID equipe1Id;
    public UUID equipe2Id;
    public Integer piste;
    public LocalTime startTime;
    public MatchStatus statut;
    public String score1;
    public String score2;
}
