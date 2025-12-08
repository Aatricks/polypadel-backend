package com.polypadel.events.web;

import com.polypadel.events.dto.EventCreateRequest;
import com.polypadel.events.dto.EventResponse;
import com.polypadel.events.dto.EventUpdateRequest;
import com.polypadel.events.service.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // <--- Import Sécurité
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/events") // L'URL finale sera /api/v1/events
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Endpoint spécifique pour le composant Calendrier du Frontend
     * URL: GET /api/v1/events/calendar?start=...&end=...
     */
    @GetMapping("/calendar")
    public ResponseEntity<List<EventResponse>> calendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(eventService.calendar(start, end));
    }

    /**
     * Liste paginée des événements (Admin ou Liste classique)
     * URL: GET /api/v1/events
     */
    @GetMapping
    public ResponseEntity<Page<EventResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(eventService.list(pageable));
    }

    /**
     * Détail d'un événement
     * URL: GET /api/v1/events/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.get(id));
    }

    /**
     * Création (Admin seulement)
     * URL: POST /api/v1/events
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventCreateRequest req) {
        return ResponseEntity.ok(eventService.create(req));
    }

    /**
     * Modification (Admin seulement)
     * URL: PUT /api/v1/events/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> update(@PathVariable UUID id, @Valid @RequestBody EventUpdateRequest req) {
        return ResponseEntity.ok(eventService.update(id, req));
    }

    /**
     * Suppression (Admin seulement)
     * URL: DELETE /api/v1/events/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}