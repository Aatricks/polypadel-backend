package com.polypadel.matches.dto;

import com.polypadel.domain.enums.MatchStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class MatchResponse {
    public UUID id;
    public UUID evenementId;
    public UUID equipe1Id;
    public UUID equipe2Id;
    
    // --- AJOUTS ---
    public TeamDto team1; // L'objet complet pour l'affichage
    public TeamDto team2;
    // --------------

    public Integer piste;
    public LocalDateTime startTime;
    public MatchStatus statut;
    public String score1;
    public String score2;
}