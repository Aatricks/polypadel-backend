package com.polypadel.rankings.web;

import com.polypadel.rankings.dto.RankingRow;
import com.polypadel.rankings.service.RankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/rankings/poule/{pouleId}")
    public ResponseEntity<List<RankingRow>> rankingForPoule(@PathVariable UUID pouleId) {
        return ResponseEntity.ok(rankingService.rankingForPoule(pouleId));
    }
}
