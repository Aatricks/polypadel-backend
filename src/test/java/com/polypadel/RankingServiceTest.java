package com.polypadel;

import com.polypadel.dto.RankingRow;
import com.polypadel.service.RankingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RankingServiceTest {

    @Autowired
    private RankingService rankingService;

    @Test
    void getRankings() {
        List<RankingRow> rankings = rankingService.getRankings();
        assertNotNull(rankings);
    }
}
