package com.polypadel.poules.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polypadel.poules.dto.PouleCreateRequest;
import com.polypadel.poules.dto.PouleResponse;
import com.polypadel.poules.dto.PouleUpdateRequest;
import com.polypadel.poules.service.PouleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PouleController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PouleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PouleService pouleService;

    @MockBean
    private com.polypadel.security.JwtService jwtService;
    @MockBean
    private com.polypadel.auth.repository.JSONTokenRepository jsonTokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void list_returns_ok() throws Exception {
        when(pouleService.list(any())).thenReturn(new PageImpl<>(List.of(new PouleResponse(UUID.randomUUID(), "P1", 6))));
        mockMvc.perform(get("/admin/poules"))
                .andExpect(status().isOk());
    }

    @Test
    public void create_returns_ok() throws Exception {
        PouleCreateRequest req = new PouleCreateRequest("P1");
        when(pouleService.create(any())).thenReturn(new PouleResponse(UUID.randomUUID(), "P1", 6));
        mockMvc.perform(post("/admin/poules").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nom").value("P1"));
    }

    @Test
    public void get_returns_ok() throws Exception {
        UUID id = UUID.randomUUID();
        when(pouleService.get(id)).thenReturn(new PouleResponse(id, "P1", 6));
        mockMvc.perform(get("/admin/poules/" + id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    public void update_returns_ok() throws Exception {
        UUID id = UUID.randomUUID();
        PouleUpdateRequest req = new PouleUpdateRequest("P1");
        when(pouleService.update(any(), any())).thenReturn(new PouleResponse(id, "P1", 6));
        mockMvc.perform(put("/admin/poules/" + id).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    public void delete_returns_no_content() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(pouleService).delete(id);
        mockMvc.perform(delete("/admin/poules/" + id)).andExpect(status().isNoContent());
    }

    @Test
    public void assign_and_remove_team_returns_ok() throws Exception {
        UUID pouleId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        doNothing().when(pouleService).assignTeam(pouleId, teamId);
        doNothing().when(pouleService).removeTeam(pouleId, teamId);
        mockMvc.perform(post("/admin/poules/" + pouleId + "/assign/" + teamId)).andExpect(status().isOk());
        mockMvc.perform(post("/admin/poules/" + pouleId + "/remove/" + teamId)).andExpect(status().isOk());
    }
}
