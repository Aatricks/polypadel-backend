package com.polypadel.controller;

import com.polypadel.dto.RankingRow;
import com.polypadel.dto.MyResultsResponse;
import com.polypadel.model.User;
import com.polypadel.service.RankingService;
import com.polypadel.service.ResultsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/results")
public class ResultsController {
    private final RankingService rankingService;
    private final ResultsService resultsService;

    public ResultsController(RankingService rankingService, ResultsService resultsService) {
        this.rankingService = rankingService;
        this.resultsService = resultsService;
    }

    @GetMapping("/rankings")
    public ResponseEntity<Map<String, Object>> getRankings() {
        List<RankingRow> rankings = rankingService.getRankings();
        return ResponseEntity.ok(Map.of("rankings", rankings));
    }

    @GetMapping("/my-results")
    public ResponseEntity<MyResultsResponse> getMyResults(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(resultsService.getMyResults(user));
    }
}
