package com.polypadel.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record MatchResponse(
    Long id,
    EventInfo event,
    int courtNumber,
    TeamResponse team1,
    TeamResponse team2,
    String status,
    String scoreTeam1,
    String scoreTeam2
) {
    public record EventInfo(LocalDate date, LocalTime time) {}
}
