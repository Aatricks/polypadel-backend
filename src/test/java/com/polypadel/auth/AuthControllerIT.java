package com.polypadel.auth;

import com.polypadel.domain.entity.Utilisateur;
import com.polypadel.domain.enums.Role;
import com.polypadel.users.repository.UtilisateurRepository;
import com.polypadel.testsupport.PostgresTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerIT extends PostgresTest {

    @Autowired
    WebApplicationContext wac;

    @Autowired
    UtilisateurRepository utilisateurRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        utilisateurRepository.deleteAll();
        Utilisateur u = new Utilisateur();
        u.setEmail("user@example.com");
        u.setEmailHash("hash");
        u.setPasswordHash(passwordEncoder.encode("Password1!"));
        u.setRole(Role.JOUEUR);
        u.setActive(true);
        utilisateurRepository.save(u);
    }

    @Test
    void login_success_sets_cookie_and_returns_token() throws Exception {
        String body = "{\"email\":\"user@example.com\",\"password\":\"Password1!\"}";
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.email").value("user@example.com"));
    }

    @Test
    void login_invalid_credentials() throws Exception {
        String body = "{\"email\":\"user@example.com\",\"password\":\"Wrong\"}";
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("AUTH_INVALID_CREDENTIALS"));
    }

        @Test
        void login_locks_after_three_failed_attempts() throws Exception {
        String body = "{\"email\":\"user@example.com\",\"password\":\"Wrong\"}";

        // 1st attempt
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.getBytes(StandardCharsets.UTF_8)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("AUTH_INVALID_CREDENTIALS"));

        // 2nd attempt
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.getBytes(StandardCharsets.UTF_8)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("AUTH_INVALID_CREDENTIALS"));

        // 3rd attempt -> locked
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.getBytes(StandardCharsets.UTF_8)))
            .andExpect(status().isLocked())
            .andExpect(jsonPath("$.code").value("AUTH_ACCOUNT_LOCKED"));
        }

        @Test
        void login_allows_after_lockout_expiry() throws Exception {
        // Lock the account by direct modification, set lockoutUntil to past
        Utilisateur u = utilisateurRepository.findByEmail("user@example.com").orElseThrow();
        u.setFailedLoginAttempts(3);
        u.setLockoutUntil(java.time.Instant.now().minus(java.time.Duration.ofMinutes(1)));
        utilisateurRepository.save(u);

        // Now try with correct password
        String body = "{\"email\":\"user@example.com\",\"password\":\"Password1!\"}";
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.getBytes(StandardCharsets.UTF_8)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists());
        }
}
