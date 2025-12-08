package com.polypadel.dto;

public record RankingRow(
    int position,
    String company,
    int matchesPlayed,
    int wins,
    int losses,
    int points,
    int setsWon,
    int setsLost
) {}
