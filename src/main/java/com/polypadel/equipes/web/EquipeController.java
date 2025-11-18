package com.polypadel.equipes.web;

import com.polypadel.equipes.dto.TeamCreateRequest;
import com.polypadel.equipes.dto.TeamResponse;
import com.polypadel.equipes.dto.TeamUpdateRequest;
import com.polypadel.equipes.repository.EquipeRepository;
import com.polypadel.equipes.service.EquipeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/teams")
public class EquipeController {

    private final EquipeService equipeService;
    private final EquipeRepository equipeRepository;

    public EquipeController(EquipeService equipeService, EquipeRepository equipeRepository) {
        this.equipeService = equipeService;
        this.equipeRepository = equipeRepository;
    }

    @GetMapping
    public ResponseEntity<Page<TeamResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(
                equipeRepository.findAll(pageable)
                        .map(e -> equipeService.get(e.getId()))
        );
    }

    @PostMapping
    public ResponseEntity<TeamResponse> create(@RequestBody TeamCreateRequest req) {
        return ResponseEntity.ok(equipeService.create(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(equipeService.get(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamResponse> update(@PathVariable UUID id, @RequestBody TeamUpdateRequest req) {
        return ResponseEntity.ok(equipeService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        equipeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
