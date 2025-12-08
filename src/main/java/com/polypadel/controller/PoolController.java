package com.polypadel.controller;

import com.polypadel.dto.*;
import com.polypadel.service.PoolService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pools")
public class PoolController {
    private final PoolService poolService;

    public PoolController(PoolService poolService) {
        this.poolService = poolService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> findAll() {
        List<PoolResponse> pools = poolService.findAll();
        return ResponseEntity.ok(Map.of("pools", pools));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PoolResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(poolService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PoolResponse> create(@Valid @RequestBody PoolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(poolService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PoolResponse> update(@PathVariable Long id, @Valid @RequestBody PoolRequest request) {
        return ResponseEntity.ok(poolService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        poolService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
