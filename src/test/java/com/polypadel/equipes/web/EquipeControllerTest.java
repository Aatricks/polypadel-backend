package com.polypadel.equipes.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polypadel.equipes.dto.TeamCreateRequest;
import com.polypadel.equipes.dto.TeamResponse;
import com.polypadel.equipes.dto.TeamUpdateRequest;
import com.polypadel.equipes.service.EquipeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EquipeController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
public class EquipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EquipeService equipeService;

    // Security beans required for Jwt filter autowiring in the test context
    @MockBean
    private com.polypadel.security.JwtService jwtService;
    @MockBean
    private com.polypadel.auth.repository.JSONTokenRepository jsonTokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void list_returns_ok() throws Exception {
        var team = new TeamResponse(UUID.randomUUID(), "Ent1", null, UUID.randomUUID(), UUID.randomUUID());
        when(equipeService.list(any())).thenReturn(new PageImpl<>(List.of(team)));

        mockMvc.perform(get("/admin/teams"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void create_returns_ok() throws Exception {
        TeamCreateRequest req = new TeamCreateRequest("Ent1", UUID.randomUUID(), UUID.randomUUID(), null);
        when(equipeService.create(any())).thenReturn(new TeamResponse(UUID.randomUUID(), "Ent1", null, req.joueur1Id(), req.joueur2Id()));

        mockMvc.perform(post("/admin/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entreprise").value("Ent1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void get_returns_ok() throws Exception {
        UUID id = UUID.randomUUID();
        when(equipeService.get(id)).thenReturn(new TeamResponse(id, "Ent1", null, UUID.randomUUID(), UUID.randomUUID()));

        mockMvc.perform(get("/admin/teams/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void update_returns_ok() throws Exception {
        UUID id = UUID.randomUUID();
        TeamUpdateRequest req = new TeamUpdateRequest("Ent1", null, null, null);
        when(equipeService.update(any(), any())).thenReturn(new TeamResponse(id, "Ent1", null, UUID.randomUUID(), UUID.randomUUID()));

        mockMvc.perform(put("/admin/teams/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void delete_returns_no_content() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(equipeService).delete(id);

        mockMvc.perform(delete("/admin/teams/" + id))
                .andExpect(status().isNoContent());
    }
}
