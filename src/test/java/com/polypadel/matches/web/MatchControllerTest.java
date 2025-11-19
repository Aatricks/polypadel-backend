package com.polypadel.matches.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polypadel.matches.dto.MatchCreateRequest;
import com.polypadel.matches.dto.MatchResponse;
import com.polypadel.matches.dto.MatchUpdateScoreRequest;
import com.polypadel.matches.service.MatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MatchController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
public class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchService matchService;

    @MockBean
    private com.polypadel.security.JwtService jwtService;
    @MockBean
    private com.polypadel.auth.repository.JSONTokenRepository jsonTokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void create_return_ok() throws Exception {
        MatchCreateRequest req = new MatchCreateRequest(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 1, LocalTime.of(10, 0));
        MatchResponse resp = new MatchResponse(UUID.randomUUID(), req.evenementId(), req.equipe1Id(), req.equipe2Id(), req.piste(), req.startTime(), null, null, null);
        when(matchService.create(org.mockito.ArgumentMatchers.any())).thenReturn(resp);

        mockMvc.perform(post("/admin/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$.evenementId").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updateScore_returns_ok() throws Exception {
        UUID id = UUID.randomUUID();
        MatchUpdateScoreRequest req = new MatchUpdateScoreRequest("6-4 6-4", "4-6 6-3", com.polypadel.domain.enums.MatchStatus.TERMINE);
        MatchResponse resp = new MatchResponse(id, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 1, LocalTime.of(10, 0), com.polypadel.domain.enums.MatchStatus.TERMINE, "6-4 6-4", "4-6 6-3");
        when(matchService.updateScore(org.mockito.ArgumentMatchers.eq(id), org.mockito.ArgumentMatchers.any())).thenReturn(resp);

        mockMvc.perform(put("/admin/matches/" + id + "/score")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "JOUEUR")
    public void upcoming_for_current_user_is_ok() throws Exception {
        when(matchService.upcomingForCurrentUser()).thenReturn(List.of());

        mockMvc.perform(get("/matches/upcoming"))
                .andExpect(status().isOk());
    }

    @Test
    public void list_by_event_is_ok() throws Exception {
        UUID eventId = UUID.randomUUID();
        when(matchService.listByEvent(eventId)).thenReturn(List.of());

        mockMvc.perform(get("/matches/public/event/" + eventId))
                .andExpect(status().isOk());
    }
}
