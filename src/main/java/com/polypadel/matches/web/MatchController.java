package com.polypadel.matches.web;

import com.polypadel.matches.dto.MatchCreateRequest;
import com.polypadel.matches.dto.MatchResponse;
import com.polypadel.matches.dto.MatchUpdateRequest; // Attention: On utilisera ce nouveau nom de DTO
import com.polypadel.matches.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Pour sécuriser les méthodes Admin
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/matches") // L'URL finale sera /api/v1/matches
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    /**
     * Recherche de matchs avec filtres (Frontend: fetchMatches)
     * URL: GET /api/v1/matches?upcoming=true&my_matches=false...
     */
    @GetMapping
    public ResponseEntity<List<MatchResponse>> listMatches(
            @RequestParam(required = false) boolean upcoming,
            @RequestParam(name = "my_matches", required = false) boolean myMatches,
            @RequestParam(name = "team_id", required = false) UUID teamId,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(matchService.searchMatches(upcoming, myMatches, teamId, status));
    }

    /**
     * Création d'un match (Admin seulement)
     * URL: POST /api/v1/matches
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MatchResponse> create(@Valid @RequestBody MatchCreateRequest req) {
        return ResponseEntity.ok(matchService.create(req));
    }

    /**
     * Mise à jour (Score, Statut ou Piste) (Admin seulement)
     * URL: PUT /api/v1/matches/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MatchResponse> update(@PathVariable UUID id, @RequestBody MatchUpdateRequest req) {
        return ResponseEntity.ok(matchService.update(id, req));
    }

    /**
     * Suppression d'un match (Admin seulement)
     * URL: DELETE /api/v1/matches/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        matchService.delete(id);
        return ResponseEntity.noContent().build();
    }
}