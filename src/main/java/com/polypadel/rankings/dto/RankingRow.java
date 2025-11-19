package com.polypadel.rankings.dto;

import java.util.UUID;

public record RankingRow(
    UUID teamId,
    String entreprise,
    int played,
    int wins,
    int losses,
    int setsFor,
    int setsAgainst,
    int gamesFor,
    int gamesAgainst
) implements Comparable<RankingRow> {

    public int points() {
        return wins; // 1 point per win
    }

    public int setDiff() { return setsFor - setsAgainst; }
    public int gameDiff() { return gamesFor - gamesAgainst; }

    @Override
    public int compareTo(RankingRow o) {
        int c = Integer.compare(o.points(), this.points());
        if (c != 0) return c;
        c = Integer.compare(o.setDiff(), this.setDiff());
        if (c != 0) return c;
        return Integer.compare(o.gameDiff(), this.gameDiff());
    }
}
