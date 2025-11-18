package com.polypadel.matches.web;

import com.polypadel.matches.dto.MatchCreateRequest;
import com.polypadel.matches.dto.MatchResponse;
import com.polypadel.matches.dto.MatchUpdateScoreRequest;
import com.polypadel.matches.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping("/admin/matches")
    public ResponseEntity<MatchResponse> create(@Valid @RequestBody MatchCreateRequest req) {
        return ResponseEntity.ok(matchService.create(req));
    }

    @PutMapping("/admin/matches/{id}/score")
    public ResponseEntity<MatchResponse> updateScore(@PathVariable UUID id, @RequestBody MatchUpdateScoreRequest req) {
        return ResponseEntity.ok(matchService.updateScore(id, req));
    }

    @GetMapping("/matches/upcoming")
    public ResponseEntity<List<MatchResponse>> upcomingForCurrentUser() {
        return ResponseEntity.ok(matchService.upcomingForCurrentUser());
    }

    @GetMapping("/matches/public/event/{eventId}")
    public ResponseEntity<List<MatchResponse>> listByEvent(@PathVariable UUID eventId) {
        return ResponseEntity.ok(matchService.listByEvent(eventId));
    }
}
