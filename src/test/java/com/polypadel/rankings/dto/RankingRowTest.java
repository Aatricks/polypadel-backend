package com.polypadel.rankings.dto;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class RankingRowTest {

    @Test
    public void compareTo_various_cases() {
        UUID t1 = UUID.randomUUID();
        UUID t2 = UUID.randomUUID();

        // Different points -> ordering by points
        RankingRow a = new RankingRow(t1, "E", 1, 2, 0, 4, 1, 20, 10);
        RankingRow b = new RankingRow(t2, "E", 1, 1, 1, 3, 2, 18, 17);
        assertThat(a.compareTo(b)).isLessThan(0); // a has more points -> a comes first

        // Equal points -> compare set difference
        RankingRow c = new RankingRow(t1, "E", 2, 1, 1, 5, 3, 20, 18); // setDiff=2
        RankingRow d = new RankingRow(t2, "E", 2, 1, 1, 4, 4, 22, 18); // setDiff=0
        assertThat(c.compareTo(d)).isLessThan(0);

        // Equal points & setDiff -> compare gameDiff
        RankingRow e = new RankingRow(t1, "E", 2, 1, 1, 3, 3, 20, 18); // gameDiff=2
        RankingRow f = new RankingRow(t2, "E", 2, 1, 1, 3, 3, 19, 18); // gameDiff=1
        assertThat(e.compareTo(f)).isLessThan(0);
    }
}
