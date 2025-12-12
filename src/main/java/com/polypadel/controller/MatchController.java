package com.polypadel.controller;

import com.polypadel.dto.*;
import com.polypadel.model.User;
import com.polypadel.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/matches")
public class MatchController {
    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> findUpcoming(
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Boolean myMatches,
            @AuthenticationPrincipal User user) {
        List<MatchResponse> matches = matchService.findUpcoming(teamId, myMatches, user);
        return ResponseEntity.ok(Map.of("matches", matches, "total", matches.size()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.findById(id));
    }

    @PostMapping
    public ResponseEntity<MatchResponse> create(@Valid @RequestBody MatchCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matchService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MatchResponse> update(@PathVariable Long id, @Valid @RequestBody MatchUpdateRequest request) {
        return ResponseEntity.ok(matchService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        matchService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
