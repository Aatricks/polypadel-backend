package com.polypadel.matches.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polypadel.domain.enums.MatchStatus;
import com.polypadel.matches.dto.MatchCreateRequest;
import com.polypadel.matches.dto.MatchResponse;
import com.polypadel.matches.dto.MatchUpdateRequest; // <--- Nouveau DTO
import com.polypadel.matches.service.MatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MatchController.class)
@AutoConfigureMockMvc(addFilters = false) // Désactive la sécurité pour le test
public class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchService matchService;

    // Mocks de sécurité nécessaires pour le contexte
    @MockBean
    private com.polypadel.security.JwtService jwtService;
    @MockBean
    private com.polypadel.auth.repository.JSONTokenRepository jsonTokenRepository;
    @MockBean
    private com.polypadel.security.handlers.RestAuthenticationEntryPoint entryPoint;
    @MockBean
    private com.polypadel.security.handlers.RestAccessDeniedHandler deniedHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void create_return_ok() throws Exception {
        // Given : Nouveau DTO avec LocalDateTime et Piste
        MatchCreateRequest req = new MatchCreateRequest(
            UUID.randomUUID(), 
            UUID.randomUUID(), 
            UUID.randomUUID(), 
            1, 
            LocalDateTime.now().plusDays(1) // <--- LocalDateTime
        );

        MatchResponse resp = new MatchResponse(); // On suppose un constructeur vide ou setters
        resp.id = UUID.randomUUID();

        when(matchService.create(any())).thenReturn(resp);

        // When : POST /matches (Plus de /admin/matches)
        mockMvc.perform(post("/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    public void update_returns_ok() throws Exception {
        UUID id = UUID.randomUUID();
        
        // Given : Nouveau DTO Update (Score, Statut, Piste)
        MatchUpdateRequest req = new MatchUpdateRequest(
            "6-4, 6-4", 
            "4-6, 6-3", 
            MatchStatus.TERMINE, 
            2 // changement de piste
        );
        
        MatchResponse resp = new MatchResponse();
        resp.id = id;

        // Note: La méthode du service est maintenant 'update', pas 'updateScore'
        when(matchService.update(eq(id), any())).thenReturn(resp);

        // When : PUT /matches/{id} (Plus de /score)
        mockMvc.perform(put("/matches/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    public void list_matches_with_filters_is_ok() throws Exception {
        // Test de la recherche globale (remplace upcomingForCurrentUser)
        
        when(matchService.searchMatches(true, true, null, null)).thenReturn(List.of());

        // When : GET /matches?upcoming=true&my_matches=true
        mockMvc.perform(get("/matches")
                        .param("upcoming", "true")
                        .param("my_matches", "true"))
                .andExpect(status().isOk());
    }

    @Test
    public void delete_returns_no_content() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(matchService).delete(id);

        // When : DELETE /matches/{id}
        mockMvc.perform(delete("/matches/" + id))
                .andExpect(status().isNoContent());
    }
}