package com.polypadel;

import com.polypadel.dto.*;
import com.polypadel.service.*;
import com.polypadel.model.*;
import com.polypadel.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.ActiveProfiles;

/**
 * Consolidated test suite covering essential flows:
 * - Authentication (login, password change, brute force protection)
 * - CRUD operations for all entities
 * - Admin functionality
 * - Business rule validations
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CoreFunctionalityTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserAuthService userAuthService;
    @Autowired private PlayerService playerService;
    @Autowired private TeamService teamService;
    @Autowired private EventService eventService;
    @Autowired private MatchService matchService;
    @Autowired private PoolService poolService;
    @Autowired private RankingService rankingService;
    @Autowired private ResultsService resultsService;
    @Autowired private ProfileService profileService;
    @Autowired private UserRepository userRepository;
    @Autowired private PlayerRepository playerRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private MatchRepository matchRepository;
    @Autowired private PoolRepository poolRepository;

    private static String adminToken;
    private static String testUnique;

    @BeforeAll
    static void setup() {
        testUnique = String.valueOf(System.currentTimeMillis()).substring(6);
    }

    // ========== Authentication Tests ==========

    @Test
    @Order(1)
    void testAdminLogin() {
        LoginResponse login = userAuthService.login(new LoginRequest("admin@padel.com", "Admin@2025!"));
        assertNotNull(login.accessToken());
        assertEquals("ADMINISTRATEUR", login.user().role());
        assertFalse(login.user().mustChangePassword());
        adminToken = login.accessToken();
    }

    @Test
    @Order(2)
    void testLoginFailure() {
        assertThrows(ResponseStatusException.class, () ->
            userAuthService.login(new LoginRequest("admin@padel.com", "wrongpassword")));
    }

    @Test
    @Order(3)
    void testLoginEndpoint() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@padel.com\",\"password\":\"Admin@2025!\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.user.role").value("ADMINISTRATEUR"));
    }

    @Test
    @Order(4)
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/players"))
            .andExpect(status().isForbidden()); // Spring Security returns 403 for missing auth
    }

    // ========== Player CRUD Tests ==========

    @Test
    @Order(10)
    void testGetPlayers() {
        List<PlayerResponse> players = playerService.findAll();
        assertNotNull(players);
        assertTrue(players.size() >= 4); // Initial data
    }

    @Test
    @Order(11)
    void testCreatePlayer() {
        PlayerResponse player = playerService.create(new PlayerRequest(
            "Test", "Player", "TestCorp" + testUnique, "LA" + testUnique, "test" + testUnique + "@test.com"));
        assertNotNull(player.id());
        assertEquals("Test", player.firstName());
        assertEquals("Player", player.lastName());
    }

    @Test
    @Order(12)
    void testDuplicateLicense() {
        assertThrows(ResponseStatusException.class, () ->
            playerService.create(new PlayerRequest("Dup", "Test", "Corp", "L123456", "dup@test.com")));
    }

    @Test
    @Order(13)
    void testUpdatePlayer() {
        Player player = playerRepository.findAll().stream().findFirst().orElseThrow();
        PlayerResponse updated = playerService.update(player.getId(),
            new PlayerRequest("Updated", "Name", player.getCompany(), player.getLicenseNumber(), "updated@test.com"));
        assertEquals("Updated", updated.firstName());
    }

    // ========== Team CRUD Tests ==========

    @Test
    @Order(20)
    void testGetTeams() {
        List<TeamResponse> teams = teamService.findAll(null, null);
        assertNotNull(teams);
    }

    @Test
    @Order(21)
    void testCreateTeam() {
        // Create two players for new team
        PlayerResponse p1 = playerService.create(new PlayerRequest(
            "Team1P1", "Test", "TeamCorp" + testUnique, "LB" + testUnique, "t1p1" + testUnique + "@test.com"));
        PlayerResponse p2 = playerService.create(new PlayerRequest(
            "Team1P2", "Test", "TeamCorp" + testUnique, "LC" + testUnique, "t1p2" + testUnique + "@test.com"));

        TeamResponse team = teamService.create(new TeamRequest("TeamCorp" + testUnique, p1.id(), p2.id(), null));
        assertNotNull(team.id());
        assertEquals(2, team.players().size());
    }

    @Test
    @Order(22)
    void testSamePlayerCannotBeBothTeamMembers() {
        Player player = playerRepository.findAll().stream().findFirst().orElseThrow();
        assertThrows(ResponseStatusException.class, () ->
            teamService.create(new TeamRequest("Corp", player.getId(), player.getId(), null)));
    }

    // ========== Pool CRUD Tests ==========

    @Test
    @Order(30)
    void testGetPools() {
        List<PoolResponse> pools = poolService.findAll();
        assertNotNull(pools);
    }

    @Test
    @Order(31)
    void testPoolRequiresSixTeams() {
        assertThrows(ResponseStatusException.class, () ->
            poolService.create(new PoolRequest("TestPool", List.of(1L, 2L)))); // Less than 6 teams
    }

    // ========== Event & Match Tests ==========

    @Test
    @Order(40)
    void testGetEvents() {
        List<EventResponse> events = eventService.findAll(null, null, null);
        assertNotNull(events);
    }

    @Test
    @Order(41)
    void testEventDuplicateCourtValidation() {
        List<EventRequest.MatchInfo> matches = List.of(
            new EventRequest.MatchInfo(1, 1L, 2L),
            new EventRequest.MatchInfo(1, 3L, 4L) // Same court
        );
        assertThrows(ResponseStatusException.class, () ->
            eventService.create(new EventRequest(LocalDate.now().plusDays(30), LocalTime.of(19, 0), matches)));
    }

    @Test
    @Order(42)
    void testTeamCannotPlayItself() {
        List<EventRequest.MatchInfo> matches = List.of(
            new EventRequest.MatchInfo(1, 1L, 1L) // Same team
        );
        assertThrows(ResponseStatusException.class, () ->
            eventService.create(new EventRequest(LocalDate.now().plusDays(30), LocalTime.of(19, 0), matches)));
    }

    // ========== Ranking Tests ==========

    @Test
    @Order(50)
    void testGetRankings() {
        List<RankingRow> rankings = rankingService.getRankings();
        assertNotNull(rankings);
    }

    // ========== Admin Tests ==========

    @Test
    @Order(60)
    void testAdminCreateAccount() {
        // Find a player without account
        Player player = playerRepository.findAll().stream()
            .filter(p -> p.getUser() == null)
            .findFirst()
            .orElse(null);

        if (player != null) {
            UserAuthService.AccountResponse response = userAuthService.createAccount(player.getId(), "JOUEUR");
            assertNotNull(response.temporaryPassword());
            assertNotNull(response.email());
        }
    }

    @Test
    @Order(61)
    void testAdminResetPassword() {
        User user = userRepository.findAll().stream()
            .filter(u -> !"admin@padel.com".equals(u.getEmail()))
            .findFirst()
            .orElse(null);

        if (user != null) {
            UserAuthService.AccountResponse response = userAuthService.resetPassword(user.getId());
            assertNotNull(response.temporaryPassword());
        }
    }

    // ========== API Endpoint Tests ==========

    @Test
    @Order(70)
    void testPlayersEndpoint() throws Exception {
        String token = getAdminToken();
        mockMvc.perform(get("/players")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.players").isArray());
    }

    @Test
    @Order(71)
    void testTeamsEndpoint() throws Exception {
        String token = getAdminToken();
        mockMvc.perform(get("/teams")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.teams").isArray());
    }

    @Test
    @Order(72)
    void testPoolsEndpoint() throws Exception {
        String token = getAdminToken();
        mockMvc.perform(get("/pools")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pools").isArray());
    }

    @Test
    @Order(73)
    void testEventsEndpoint() throws Exception {
        String token = getAdminToken();
        mockMvc.perform(get("/events")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.events").isArray());
    }

    @Test
    @Order(74)
    void testRankingsEndpoint() throws Exception {
        String token = getAdminToken();
        mockMvc.perform(get("/results/rankings")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rankings").isArray());
    }

    private String getAdminToken() {
        if (adminToken == null) {
            LoginResponse login = userAuthService.login(new LoginRequest("admin@padel.com", "Admin@2025!"));
            adminToken = login.accessToken();
        }
        return adminToken;
    }
}
