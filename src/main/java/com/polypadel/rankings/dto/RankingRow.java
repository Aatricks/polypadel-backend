package com.polypadel.rankings.dto;

import java.util.UUID;

public class RankingRow implements Comparable<RankingRow> {
    public UUID teamId;
    public String entreprise;
    public int played;
    public int wins;
    public int losses;
    public int setsFor;
    public int setsAgainst;
    public int gamesFor;
    public int gamesAgainst;

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
