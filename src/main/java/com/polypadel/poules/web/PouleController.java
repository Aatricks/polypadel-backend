package com.polypadel.poules.web;

import com.polypadel.poules.dto.PouleCreateRequest;
import com.polypadel.poules.dto.PouleResponse;
import com.polypadel.poules.dto.PouleUpdateRequest;
import com.polypadel.poules.service.PouleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/poules")
public class PouleController {

    private final PouleService pouleService;

    public PouleController(PouleService pouleService) {
        this.pouleService = pouleService;
    }

    @GetMapping
    public ResponseEntity<Page<PouleResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(pouleService.list(pageable));
    }

    @PostMapping
    public ResponseEntity<PouleResponse> create(@Valid @RequestBody PouleCreateRequest req) {
        return ResponseEntity.ok(pouleService.create(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PouleResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(pouleService.get(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PouleResponse> update(@PathVariable UUID id, @Valid @RequestBody PouleUpdateRequest req) {
        return ResponseEntity.ok(pouleService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        pouleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{pouleId}/assign/{teamId}")
    public ResponseEntity<Void> assignTeam(@PathVariable UUID pouleId, @PathVariable UUID teamId) {
        pouleService.assignTeam(pouleId, teamId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{pouleId}/remove/{teamId}")
    public ResponseEntity<Void> removeTeam(@PathVariable UUID pouleId, @PathVariable UUID teamId) {
        pouleService.removeTeam(pouleId, teamId);
        return ResponseEntity.ok().build();
    }
}
