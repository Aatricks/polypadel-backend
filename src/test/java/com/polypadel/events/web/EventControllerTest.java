package com.polypadel.events.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polypadel.events.dto.EventCreateRequest;
import com.polypadel.events.dto.EventResponse;
import com.polypadel.events.dto.EventUpdateRequest;
import com.polypadel.events.service.EventService;
import com.polypadel.matches.dto.MatchResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EventController.class)
@AutoConfigureMockMvc(addFilters = false) // Désactive la sécurité (JWT) pour ce test
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    // Mocks nécessaires pour que le contexte Spring démarre (dépendances de SecurityConfig)
    @MockBean
    private com.polypadel.security.JwtService jwtService;
    
    // Attention au package ici (vérifiez s'il est dans .repository ou .auth.repository)
    @MockBean
    private com.polypadel.auth.repository.JSONTokenRepository jsonTokenRepository; 
    
    @MockBean
    private com.polypadel.security.handlers.RestAuthenticationEntryPoint entryPoint;
    @MockBean
    private com.polypadel.security.handlers.RestAccessDeniedHandler deniedHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void calendar_returns_ok() throws Exception {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(3);
        
        // Création d'une réponse valide avec les nouveaux champs
        EventResponse mockResponse = new EventResponse(
            UUID.randomUUID(), 
            start, 
            LocalTime.of(10, 0), 
            new ArrayList<>() // Liste de matchs vide
        );

        when(eventService.calendar(start, end)).thenReturn(List.of(mockResponse));

        mockMvc.perform(get("/events/calendar")
                .param("start", start.toString())
                .param("end", end.toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void crud_endpoints_work() throws Exception {
        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.of(14, 0);

        // 1. CREATE
        // Nouveau DTO : Date, Heure, Liste de matchs vide
        EventCreateRequest createReq = new EventCreateRequest(date, time, new ArrayList<>());
        EventResponse response = new EventResponse(id, date, time, new ArrayList<>());
        
        when(eventService.create(any())).thenReturn(response);
        
        // Note: L'URL est maintenant /events (le contrôleur n'a plus /admin en dur)
        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk());

        // 2. LIST
        when(eventService.list(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(response)));
        mockMvc.perform(get("/events")).andExpect(status().isOk());

        // 3. GET ONE
        when(eventService.get(id)).thenReturn(response);
        mockMvc.perform(get("/events/" + id)).andExpect(status().isOk());

        // 4. UPDATE
        // Nouveau DTO Update
        EventUpdateRequest updateReq = new EventUpdateRequest(date.plusDays(1), time);
        when(eventService.update(any(), any())).thenReturn(response);
        
        mockMvc.perform(put("/events/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk());

        // 5. DELETE
        doNothing().when(eventService).delete(id);
        mockMvc.perform(delete("/events/" + id)).andExpect(status().isNoContent());
    }
}