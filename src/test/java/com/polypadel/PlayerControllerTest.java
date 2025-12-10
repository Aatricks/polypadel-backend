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
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Test
    void getPlayersAuthenticated() throws Exception {
        String token = getAdminToken();
        mockMvc.perform(get("/players")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players").isArray());
    }

    @Test
    void getPlayersUnauthenticated() throws Exception {
        mockMvc.perform(get("/players"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPlayerById() throws Exception {
        String token = getAdminToken();
        mockMvc.perform(get("/players/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createPlayerValidation() throws Exception {
        String token = getAdminToken();
        mockMvc.perform(post("/players")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"\",\"lastName\":\"Test\",\"company\":\"Corp\",\"licenseNumber\":\"INVALID\",\"email\":\"test@test.com\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    private String getAdminToken() {
        LoginResponse response = authService.login(new LoginRequest("admin@padel.com", "Admin@2025!"));
        return response.accessToken();
    }
}
