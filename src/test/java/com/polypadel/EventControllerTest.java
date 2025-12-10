package com.polypadel;

import com.polypadel.dto.LoginRequest;
import com.polypadel.dto.LoginResponse;
import com.polypadel.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Test
    void getEvents() throws Exception {
        String token = getAdminToken();
        mockMvc.perform(get("/events")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events").isArray());
    }

    @Test
    void getEventsWithMonthFilter() throws Exception {
        String token = getAdminToken();
        mockMvc.perform(get("/events?month=2025-12")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private String getAdminToken() {
        LoginResponse response = authService.login(new LoginRequest("admin@padel.com", "Admin@2025!"));
        return response.accessToken();
    }
}
