package com.polypadel.users.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polypadel.users.dto.PasswordUpdateRequest;
import com.polypadel.users.dto.ProfileResponse;
import com.polypadel.users.dto.ProfileUpdateRequest;
import com.polypadel.users.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private com.polypadel.security.JwtService jwtService;
    @MockBean
    private com.polypadel.auth.repository.JSONTokenRepository jsonTokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getProfile_ok() throws Exception {
        var resp = new ProfileResponse(UUID.randomUUID(), "a@b.com", "JOUEUR", null, null, null, null, null, null);
        when(profileService.getProfile()).thenReturn(resp);
        mockMvc.perform(get("/profile")).andExpect(status().isOk()).andExpect(jsonPath("$.email").value("a@b.com"));
    }

    @Test
    public void updateProfile_ok() throws Exception {
        ProfileUpdateRequest req = new ProfileUpdateRequest("Nom", "Prenom", LocalDate.of(1990, 1, 1), null);
        var resp = new ProfileResponse(UUID.randomUUID(), "a@b.com", "JOUEUR", UUID.randomUUID(), "Nom", "Prenom", LocalDate.of(1990, 1, 1), null, null);
        when(profileService.updateProfile(any())).thenReturn(resp);
        mockMvc.perform(put("/profile").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req))).andExpect(status().isOk()).andExpect(jsonPath("$.nom").value("Nom"));
    }

    @Test
    public void changePassword_ok() throws Exception {
        PasswordUpdateRequest req = new PasswordUpdateRequest("oldPass", "NewPass@1");
        doNothing().when(profileService).changePassword(any());
        mockMvc.perform(put("/profile/password").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req))).andExpect(status().isNoContent());
    }
}
