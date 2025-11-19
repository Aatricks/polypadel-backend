package com.polypadel.poules;

import com.polypadel.domain.entity.Equipe;
import com.polypadel.domain.entity.Joueur;
import com.polypadel.domain.entity.Poule;
import com.polypadel.domain.entity.Utilisateur;
import com.polypadel.domain.enums.Role;
import com.polypadel.equipes.repository.EquipeRepository;
import com.polypadel.joueurs.repository.JoueurRepository;
import com.polypadel.poules.repository.PouleRepository;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PouleControllerIT extends PostgresTest {

    @Autowired
    WebApplicationContext wac;

    @Autowired
    UtilisateurRepository utilisateurRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    PouleRepository pouleRepository;

    @Autowired
    JoueurRepository joueurRepository;

    @Autowired
    EquipeRepository equipeRepository;

    MockMvc mockMvc;

    String adminCookie;

    @BeforeEach
    void setup() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        equipeRepository.deleteAll();
        joueurRepository.deleteAll();
        pouleRepository.deleteAll();
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
    void delete_empty_poule_ok_and_delete_with_team_blocked() throws Exception {
        // create poule
        MvcResult create = mockMvc.perform(post("/admin/poules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Cookie", adminCookie)
                        .content("{\"nom\":\"Poule A\"}".getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        String response = create.getResponse().getContentAsString();
        // extract id naive
        String idStr = response.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");
        UUID pouleId = UUID.fromString(idStr);

        // delete should work when empty
        mockMvc.perform(delete("/admin/poules/" + pouleId)
                        .header("Cookie", adminCookie))
                .andExpect(status().isNoContent());

        // recreate and assign a team via repository to simulate non-empty
        Poule p = new Poule();
        p.setNom("Poule B");
        p = pouleRepository.save(p);
        Joueur j1 = new Joueur();
        j1.setNumLicence("LIC1");
        j1.setNom("A");
        j1.setPrenom("B");
        j1.setDateNaissance(LocalDate.now().minusYears(20));
        j1.setEntreprise("Corp");
        j1 = joueurRepository.save(j1);
        Joueur j2 = new Joueur();
        j2.setNumLicence("LIC2");
        j2.setNom("C");
        j2.setPrenom("D");
        j2.setDateNaissance(LocalDate.now().minusYears(22));
        j2.setEntreprise("Corp");
        j2 = joueurRepository.save(j2);
        Equipe team = new Equipe();
        team.setEntreprise("Corp");
        team.setJoueur1(j1);
        team.setJoueur2(j2);
        team.setPoule(p);
        equipeRepository.save(team);

        mockMvc.perform(delete("/admin/poules/" + p.getId())
                        .header("Cookie", adminCookie))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POULE_NOT_EMPTY"));
    }
}
