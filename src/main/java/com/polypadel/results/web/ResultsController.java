package com.polypadel.results.web;

import com.polypadel.matches.dto.MatchResponse;
import com.polypadel.matches.service.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/results")
public class ResultsController {

    private final MatchService matchService;

    public ResultsController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping("/user")
    public ResponseEntity<List<MatchResponse>> userResults() {
        return ResponseEntity.ok(matchService.finishedForCurrentUser());
    }
}
