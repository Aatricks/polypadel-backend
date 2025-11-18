package com.polypadel.joueurs.web;

import com.polypadel.joueurs.dto.PlayerCreateRequest;
import com.polypadel.joueurs.dto.PlayerResponse;
import com.polypadel.joueurs.dto.PlayerUpdateRequest;
import com.polypadel.joueurs.service.JoueurService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/players")
public class JoueurController {

    private final JoueurService joueurService;

    public JoueurController(JoueurService joueurService) {
        this.joueurService = joueurService;
    }

    @GetMapping
    public ResponseEntity<Page<PlayerResponse>> list(@RequestParam(required = false) String query, Pageable pageable) {
        return ResponseEntity.ok(joueurService.search(query, pageable));
    }

    @PostMapping
    public ResponseEntity<PlayerResponse> create(@Valid @RequestBody PlayerCreateRequest req) {
        return ResponseEntity.ok(joueurService.create(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(joueurService.get(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlayerResponse> update(@PathVariable UUID id, @Valid @RequestBody PlayerUpdateRequest req) {
        return ResponseEntity.ok(joueurService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        joueurService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
