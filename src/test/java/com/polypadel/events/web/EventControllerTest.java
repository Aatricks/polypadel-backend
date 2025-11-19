package com.polypadel.events.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polypadel.events.dto.EventCreateRequest;
import com.polypadel.events.dto.EventResponse;
import com.polypadel.events.dto.EventUpdateRequest;
import com.polypadel.events.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EventController.class)
@AutoConfigureMockMvc(addFilters = false)
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private com.polypadel.security.JwtService jwtService;
    @MockBean
    private com.polypadel.auth.repository.JSONTokenRepository jsonTokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void calendar_returns_ok() throws Exception {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(3);
        when(eventService.calendar(start, end)).thenReturn(List.of(new EventResponse(UUID.randomUUID(), start, end)));

        mockMvc.perform(get("/events/calendar?start=" + start + "&end=" + end)).andExpect(status().isOk());
    }

    @Test
    public void admin_endpoints_work() throws Exception {
        EventCreateRequest createReq = new EventCreateRequest(LocalDate.now(), LocalDate.now().plusDays(1));
        UUID id = UUID.randomUUID();
        when(eventService.create(any())).thenReturn(new EventResponse(id, createReq.dateDebut(), createReq.dateFin()));
        mockMvc.perform(post("/admin/events").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(createReq))).andExpect(status().isOk());

        when(eventService.list(any())).thenReturn(org.springframework.data.domain.Page.empty());
        mockMvc.perform(get("/admin/events")).andExpect(status().isOk());

        when(eventService.get(id)).thenReturn(new EventResponse(id, createReq.dateDebut(), createReq.dateFin()));
        mockMvc.perform(get("/admin/events/" + id)).andExpect(status().isOk());

        EventUpdateRequest updateReq = new EventUpdateRequest(LocalDate.now(), LocalDate.now().plusDays(2));
        when(eventService.update(any(), any())).thenReturn(new EventResponse(id, updateReq.dateDebut(), updateReq.dateFin()));
        mockMvc.perform(put("/admin/events/" + id).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateReq))).andExpect(status().isOk());

        doNothing().when(eventService).delete(id);
        mockMvc.perform(delete("/admin/events/" + id)).andExpect(status().isNoContent());
    }
}
