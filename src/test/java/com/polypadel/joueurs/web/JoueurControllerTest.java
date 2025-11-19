package com.polypadel.joueurs.web;

import com.polypadel.joueurs.dto.PlayerCreateRequest;
import com.polypadel.joueurs.dto.PlayerResponse;
import com.polypadel.joueurs.dto.PlayerUpdateRequest;
import com.polypadel.joueurs.service.JoueurService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class JoueurControllerTest {

    private JoueurService joueurService;
    private JoueurController controller;

    @BeforeEach
    void setUp() {
        joueurService = Mockito.mock(JoueurService.class);
        controller = new JoueurController(joueurService);
    }

    @Test
    void list_returns_page() {
        PlayerResponse resp = new PlayerResponse(UUID.randomUUID(), "NUM", "N", "P", LocalDate.now().minusYears(20), null, "Co");
        Mockito.when(joueurService.search(eq(null), any())).thenReturn(new PageImpl<>(List.of(resp), PageRequest.of(0, 10), 1));
        var r = controller.list(null, PageRequest.of(0, 10));
        assertEquals(200, r.getStatusCodeValue());
        assertEquals(1, r.getBody().getTotalElements());
    }

    @Test
    void create_returns_response() {
        PlayerCreateRequest req = new PlayerCreateRequest("NUM", "N", "P", LocalDate.now().minusYears(20), null, "Co");
        PlayerResponse resp = new PlayerResponse(UUID.randomUUID(), "NUM", "N", "P", LocalDate.now().minusYears(20), null, "Co");
        Mockito.when(joueurService.create(eq(req))).thenReturn(resp);
        var r = controller.create(req);
        assertEquals(200, r.getStatusCodeValue());
        assertEquals(resp, r.getBody());
    }

    @Test
    void get_returns_response() {
        UUID id = UUID.randomUUID();
        PlayerResponse resp = new PlayerResponse(id, "NUM", "N", "P", LocalDate.now().minusYears(20), null, "Co");
        Mockito.when(joueurService.get(eq(id))).thenReturn(resp);
        var r = controller.get(id);
        assertEquals(200, r.getStatusCodeValue());
        assertEquals(resp, r.getBody());
    }

    @Test
    void update_returns_response() {
        UUID id = UUID.randomUUID();
        PlayerUpdateRequest req = new PlayerUpdateRequest("N2", null, null, null, null);
        PlayerResponse resp = new PlayerResponse(id, "NUM", "N2", "P", LocalDate.now().minusYears(20), null, "Co");
        Mockito.when(joueurService.update(eq(id), eq(req))).thenReturn(resp);
        var r = controller.update(id, req);
        assertEquals(200, r.getStatusCodeValue());
        assertEquals(resp, r.getBody());
    }

    @Test
    void delete_returns_no_content() {
        UUID id = UUID.randomUUID();
        var r = controller.delete(id);
        assertEquals(204, r.getStatusCodeValue());
        Mockito.verify(joueurService).delete(eq(id));
    }
}
