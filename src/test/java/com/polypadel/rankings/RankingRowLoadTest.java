package com.polypadel.rankings;

import org.junit.jupiter.api.Test;

public class RankingRowLoadTest {
    @Test
    void canLoadRankingRow() {
        new com.polypadel.rankings.dto.RankingRow(java.util.UUID.randomUUID(), "", 0, 0, 0, 0, 0, 0, 0);
    }
}
