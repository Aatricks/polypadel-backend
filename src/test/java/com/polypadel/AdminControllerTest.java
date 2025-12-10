package com.polypadel;

import com.polypadel.dto.LoginRequest;
import com.polypadel.dto.LoginResponse;
import com.polypadel.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Test
    void createAccountPlayerNotFound() throws Exception {
        String token = getAdminToken();
        mockMvc.perform(post("/admin/accounts/create")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"player_id\":99999,\"role\":\"JOUEUR\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void resetPasswordUserNotFound() throws Exception {
        String token = getAdminToken();
        mockMvc.perform(post("/admin/accounts/99999/reset-password")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminEndpointWithPlayerToken() throws Exception {
        String token = getPlayerToken();
        mockMvc.perform(post("/admin/accounts/create")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"player_id\":1,\"role\":\"JOUEUR\"}"))
                .andExpect(status().isForbidden());
    }

    private String getAdminToken() {
        LoginResponse response = authService.login(new LoginRequest("admin@padel.com", "Admin@2025!"));
        return response.accessToken();
    }

    private String getPlayerToken() {
        LoginResponse response = authService.login(new LoginRequest("joueur@padel.com", "Joueur@2025!"));
        return response.accessToken();
    }
}
