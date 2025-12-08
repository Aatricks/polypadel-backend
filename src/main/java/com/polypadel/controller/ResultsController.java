package com.polypadel.controller;

import com.polypadel.dto.RankingRow;
import com.polypadel.service.RankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/results")
public class ResultsController {
    private final RankingService rankingService;

    public ResultsController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/rankings")
    public ResponseEntity<Map<String, Object>> getRankings() {
        List<RankingRow> rankings = rankingService.getRankings();
        return ResponseEntity.ok(Map.of("rankings", rankings));
    }
}
