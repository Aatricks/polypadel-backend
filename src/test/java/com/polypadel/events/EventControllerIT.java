package com.polypadel.events;

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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EventControllerIT extends PostgresTest {

    @Autowired
    WebApplicationContext wac;

    @Autowired
    UtilisateurRepository utilisateurRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    MockMvc mockMvc;

    String adminCookie;

    @BeforeEach
    void setup() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        utilisateurRepository.deleteAll();
        Utilisateur admin = new Utilisateur();
        admin.setEmail("admin@example.com");
        admin.setEmailHash("hash");
        admin.setPasswordHash(passwordEncoder.encode("Password1!"));
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        utilisateurRepository.save(admin);

        String body = "{\"email\":\"admin@example.com\",\"password\":\"Password1!\"}";
        MvcResult res = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andReturn();
        adminCookie = res.getResponse().getHeader("Set-Cookie");
    }

    @Test
    void create_event_and_query_calendar() throws Exception {
        String body = String.format("{\"dateDebut\":\"%s\",\"dateFin\":\"%s\"}", LocalDate.now(), LocalDate.now().plusDays(1));
        mockMvc.perform(post("/admin/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Cookie", adminCookie)
                        .content(body.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());

        mockMvc.perform(get("/events/calendar")
                        .param("start", LocalDate.now().minusDays(1).toString())
                        .param("end", LocalDate.now().plusDays(10).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dateDebut").exists());
    }

    @Test
    void invalid_dates_return_conflict() throws Exception {
        String body = String.format("{\"dateDebut\":\"%s\",\"dateFin\":\"%s\"}", LocalDate.now(), LocalDate.now().minusDays(1));
        mockMvc.perform(post("/admin/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Cookie", adminCookie)
                        .content(body.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_DATES"));
    }
}
