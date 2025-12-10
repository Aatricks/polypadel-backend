package com.polypadel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.polypadel.dto.LoginRequest;
import com.polypadel.dto.LoginResponse;
import com.polypadel.dto.PlayerRequest;
import com.polypadel.dto.TeamRequest;
import com.polypadel.model.Player;
import com.polypadel.model.Role;
import com.polypadel.model.User;
import com.polypadel.repository.EventRepository;
import com.polypadel.repository.PlayerRepository;
import com.polypadel.repository.UserRepository;
import com.polypadel.service.AuthService;
import com.polypadel.service.PlayerService;
import com.polypadel.service.TeamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ControllerEndpointsIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private AuthService authService;
    @Autowired private PlayerService playerService;
    @Autowired private TeamService teamService;
    @Autowired private PlayerRepository playerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EventRepository eventRepository;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void authChangePasswordEndpoint() throws Exception {
        String unique = unique();
        String email = "temp" + unique + "@mail.com";
        User user = new User(email, passwordEncoder.encode("CurrentP@ss1!"), Role.JOUEUR);
        userRepository.save(user);
        String token = authService.login(new LoginRequest(email, "CurrentP@ss1!")).accessToken();

        mockMvc.perform(post("/auth/change-password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"currentPassword":"CurrentP@ss1!","newPassword":"NewStrongP@ss1!","confirmPassword":"NewStrongP@ss1!"}
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void profileUpdateAndPhotoEndpoints() throws Exception {
        String unique = unique();
        String email = "profile" + unique + "@mail.com";
        User user = new User(email, passwordEncoder.encode("ProfileP@ss1!"), Role.JOUEUR);
        userRepository.save(user);

        Player player = new Player();
        player.setFirstName("First");
        player.setLastName("Last");
        player.setCompany("Company");
        player.setLicenseNumber("LP" + unique);
        player.setUser(user);
        playerRepository.save(player);

        String token = authService.login(new LoginRequest(email, "ProfileP@ss1!")).accessToken();

        mockMvc.perform(put("/profile/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"firstName":"Updated","lastName":"User","birthDate":"1990-01-01","email":"%s"}
                """.formatted(email)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.email").value(email))
            .andExpect(jsonPath("$.player.firstName").value("Updated"));

        mockMvc.perform(post("/profile/me/change-password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"currentPassword":"ProfileP@ss1!","newPassword":"NewProfileP@ss1!","confirmPassword":"NewProfileP@ss1!"}
                """))
            .andExpect(status().isOk());

        MockMultipartFile file = new MockMultipartFile("photo", "avatar.png", "image/png", "img".getBytes());
        mockMvc.perform(multipart("/profile/me/photo").file(file)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.photo_url").exists());

        mockMvc.perform(delete("/profile/me/photo")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void resultsMyResultsEndpoint() throws Exception {
        String token = playerToken();
        mockMvc.perform(get("/results/my-results")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statistics").exists());
    }

    @Test
    void playerControllerCrud() throws Exception {
        String token = adminToken();
        String unique = unique();

        MvcResult create = mockMvc.perform(post("/players")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"firstName":"P%s","lastName":"User","company":"Corp","licenseNumber":"L%s","email":"p%s@test.com"}
                """.formatted(unique, unique, unique)))
            .andExpect(status().isCreated())
            .andReturn();
        JsonNode created = objectMapper.readTree(create.getResponse().getContentAsString());
        long playerId = created.get("id").asLong();

        mockMvc.perform(put("/players/" + playerId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"firstName":"P%s","lastName":"Updated","company":"Corp2","licenseNumber":"L%s","email":"p%s@test.com"}
                """.formatted(unique, unique, unique)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.company").value("Corp2"));

        mockMvc.perform(delete("/players/" + playerId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());
    }

    @Test
    void teamControllerCrud() throws Exception {
        String token = adminToken();
        String unique = unique();
        long p1 = createPlayer("T" + unique + "A");
        long p2 = createPlayer("T" + unique + "B");

        MvcResult create = mockMvc.perform(post("/teams")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"company":"Team%s","player1Id":%d,"player2Id":%d,"poolId":null}
                """.formatted(unique, p1, p2)))
            .andExpect(status().isCreated())
            .andReturn();
        JsonNode created = objectMapper.readTree(create.getResponse().getContentAsString());
        long teamId = created.get("id").asLong();

        mockMvc.perform(put("/teams/" + teamId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"company":"Team%s-Updated","player1Id":%d,"player2Id":%d,"poolId":null}
                """.formatted(unique, p1, p2)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.company").value("Team" + unique + "-Updated"));

        mockMvc.perform(delete("/teams/" + teamId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());
    }

    @Test
    void poolControllerCrud() throws Exception {
        String token = adminToken();
        String unique = unique();

        List<Long> teamIds = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            long p1 = createPlayer("P" + unique + "A" + i);
            long p2 = createPlayer("P" + unique + "B" + i);
            long teamId = teamService.create(new TeamRequest("PoolCorp" + unique, p1, p2, null)).id();
            teamIds.add(teamId);
        }

        String payload = objectMapper.writeValueAsString(new PoolRequestBody("Pool" + unique, teamIds));
        MvcResult create = mockMvc.perform(post("/pools")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andReturn();
        long poolId = objectMapper.readTree(create.getResponse().getContentAsString()).get("id").asLong();

        String updatePayload = objectMapper.writeValueAsString(new PoolRequestBody("Pool" + unique + "X", teamIds));
        mockMvc.perform(put("/pools/" + poolId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/pools/" + poolId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());
    }

    @Test
    void eventControllerCrud() throws Exception {
        String token = adminToken();
        String unique = unique();

        long p1 = createPlayer("E" + unique + "A");
        long p2 = createPlayer("E" + unique + "B");
        long p3 = createPlayer("E" + unique + "C");
        long p4 = createPlayer("E" + unique + "D");
        long t1 = teamService.create(new TeamRequest("EvtCorp" + unique, p1, p2, null)).id();
        long t2 = teamService.create(new TeamRequest("EvtCorp" + unique + "2", p3, p4, null)).id();

        String payload = """
            {"eventDate":"%s","eventTime":"20:00","matches":[{"courtNumber":1,"team1Id":%d,"team2Id":%d}]}
        """.formatted(LocalDate.now().plusDays(20), t1, t2);
        MvcResult create = mockMvc.perform(post("/events")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andReturn();
        long eventId = objectMapper.readTree(create.getResponse().getContentAsString()).get("id").asLong();

        String updatePayload = """
            {"eventDate":"%s","eventTime":"21:00","matches":[{"courtNumber":2,"team1Id":%d,"team2Id":%d}]}
        """.formatted(LocalDate.now().plusDays(25), t1, t2);
        mockMvc.perform(put("/events/" + eventId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/events/" + eventId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());
    }

    @Test
    void matchControllerCrud() throws Exception {
        String token = adminToken();
        String unique = unique();

        long p1 = createPlayer("M" + unique + "A");
        long p2 = createPlayer("M" + unique + "B");
        long p3 = createPlayer("M" + unique + "C");
        long p4 = createPlayer("M" + unique + "D");
        long t1 = teamService.create(new TeamRequest("MatchCorp" + unique, p1, p2, null)).id();
        long t2 = teamService.create(new TeamRequest("MatchCorp" + unique + "2", p3, p4, null)).id();

        com.polypadel.model.Event event = new com.polypadel.model.Event();
        event.setEventDate(LocalDate.now().plusDays(10));
        event.setEventTime(LocalTime.of(19, 0));
        event = eventRepository.save(event);

        String payload = """
            {"eventId":%d,"team1Id":%d,"team2Id":%d,"courtNumber":3}
        """.formatted(event.getId(), t1, t2);
        MvcResult create = mockMvc.perform(post("/matches")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andReturn();
        long matchId = objectMapper.readTree(create.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/matches/" + matchId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status":"A_VENIR","scoreTeam1":"6-4","scoreTeam2":"4-6"}
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.scoreTeam1").value("6-4"));

        mockMvc.perform(delete("/matches/" + matchId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());
    }

    private long createPlayer(String suffix) {
        return playerService.create(new PlayerRequest(
            suffix + "FN",
            suffix + "LN",
            "Corp" + suffix,
            "L" + suffix + ThreadLocalRandom.current().nextInt(1000),
            suffix.toLowerCase() + "@test.com"
        )).id();
    }

    private String adminToken() {
        return authService.login(new LoginRequest("admin@padel.com", "Admin@2025!")).accessToken();
    }

    private String playerToken() {
        return authService.login(new LoginRequest("joueur@padel.com", "Joueur@2025!")).accessToken();
    }

    private String unique() {
        return String.valueOf(System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000));
    }

    private record PoolRequestBody(String name, List<Long> teamIds) {}
}
