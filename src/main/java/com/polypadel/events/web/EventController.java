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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/events/calendar")
    public ResponseEntity<List<EventResponse>> calendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(eventService.calendar(start, end));
    }

    @GetMapping("/admin/events")
    public ResponseEntity<Page<EventResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(eventService.list(pageable));
    }

    @PostMapping("/admin/events")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventCreateRequest req) {
        return ResponseEntity.ok(eventService.create(req));
    }

    @GetMapping("/admin/events/{id}")
    public ResponseEntity<EventResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.get(id));
    }

    @PutMapping("/admin/events/{id}")
    public ResponseEntity<EventResponse> update(@PathVariable UUID id, @Valid @RequestBody EventUpdateRequest req) {
        return ResponseEntity.ok(eventService.update(id, req));
    }

    @DeleteMapping("/admin/events/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
