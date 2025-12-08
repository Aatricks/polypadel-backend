package com.polypadel;

import com.polypadel.dto.*;
import com.polypadel.service.*;
import com.polypadel.model.*;
import com.polypadel.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IntegrationTest {

    @Autowired private AuthService authService;
    @Autowired private PlayerService playerService;
    @Autowired private TeamService teamService;
    @Autowired private EventService eventService;
    @Autowired private MatchService matchService;
    @Autowired private PoolService poolService;
    @Autowired private RankingService rankingService;
    @Autowired private AdminService adminService;
    @Autowired private ProfileService profileService;
    @Autowired private UserRepository userRepository;
    @Autowired private PlayerRepository playerRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private MatchRepository matchRepository;

    @Test
    void fullWorkflow() {
        String unique = String.valueOf(System.currentTimeMillis()).substring(6);
        
        // Login as admin
        LoginResponse login = authService.login(new LoginRequest("admin@padel.com", "Admin@2025!"));
        assertNotNull(login.accessToken());
        assertEquals("ADMINISTRATEUR", login.user().role());

        // Get initial players
        List<PlayerResponse> players = playerService.findAll();
        int initialCount = players.size();
        assertTrue(initialCount >= 4);

        // Create new players
        PlayerResponse p1 = playerService.create(new PlayerRequest("Int1", "Test", "IntCorp" + unique, "L1" + unique, "int1" + unique + "@test.com"));
        PlayerResponse p2 = playerService.create(new PlayerRequest("Int2", "Test", "IntCorp" + unique, "L2" + unique, "int2" + unique + "@test.com"));
        assertEquals("Int1", p1.firstName());
        assertTrue(p1.company().startsWith("IntCorp"));

        // Create team
        TeamResponse team = teamService.create(new TeamRequest("IntCorp" + unique, p1.id(), p2.id(), null));
        assertTrue(team.company().startsWith("IntCorp"));
        assertEquals(2, team.players().size());

        // Get teams
        List<TeamResponse> teams = teamService.findAll(null, null);
        assertTrue(teams.size() >= 1);

        // Get rankings
        List<RankingRow> rankings = rankingService.getRankings();
        assertNotNull(rankings);
    }

    @Test
    void playerValidation() {
        // Test duplicate license
        assertThrows(ResponseStatusException.class, () -> 
            playerService.create(new PlayerRequest("Dup", "Test", "Corp", "L123456", "dup2@test.com")));
    }

    @Test
    void teamValidationSameCompany() {
        // Players must be from same company
        Player p1 = playerRepository.findByLicenseNumber("L123456").orElse(null);
        Player p2 = playerRepository.findByLicenseNumber("L123458").orElse(null);
        
        if (p1 != null && p2 != null && !p1.getCompany().equals(p2.getCompany())) {
            assertThrows(ResponseStatusException.class, () ->
                teamService.create(new TeamRequest("Mixed", p1.getId(), p2.getId(), null)));
        }
    }

    @Test
    void eventValidation() {
        // Duplicate court should fail
        List<EventRequest.MatchInfo> matches = List.of(
            new EventRequest.MatchInfo(1, 1L, 2L),
            new EventRequest.MatchInfo(1, 1L, 2L)
        );
        EventRequest req = new EventRequest(LocalDate.now().plusDays(30), LocalTime.of(19, 0), matches);
        assertThrows(ResponseStatusException.class, () -> eventService.create(req));
    }

    @Test
    void adminAccountCreation() {
        // Try to create account for non-existent player
        assertThrows(ResponseStatusException.class, () -> 
            adminService.createAccount(99999L, "JOUEUR"));
    }

    @Test
    void profileAccess() {
        User admin = userRepository.findByEmail("admin@padel.com").orElseThrow();
        ProfileResponse profile = profileService.getProfile(admin);
        assertEquals("admin@padel.com", profile.user().email());
    }

    @Test
    void matchStatusUpdate() {
        // Find a match or skip test
        List<Match> matches = matchRepository.findAll();
        if (!matches.isEmpty()) {
            Match m = matches.get(0);
            MatchResponse updated = matchService.update(m.getId(), 
                new MatchUpdateRequest("TERMINE", "6-4, 6-2", "4-6, 2-6"));
            assertEquals("TERMINE", updated.status());
        }
    }
}
