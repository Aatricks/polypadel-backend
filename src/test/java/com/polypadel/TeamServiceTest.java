package com.polypadel;

import com.polypadel.dto.TeamRequest;
import com.polypadel.dto.TeamResponse;
import com.polypadel.service.TeamService;
import com.polypadel.service.PlayerService;
import com.polypadel.dto.PlayerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TeamServiceTest {

    @Autowired
    private TeamService teamService;

    @Autowired
    private PlayerService playerService;

    private Long player1Id;
    private Long player2Id;

    @BeforeEach
    void setup() {
        // Create test players
        try {
            var p1 = playerService.create(new PlayerRequest("Team1", "Player1", "TeamTestCorp", "L777771", "team1@test.com"));
            var p2 = playerService.create(new PlayerRequest("Team2", "Player2", "TeamTestCorp", "L777772", "team2@test.com"));
            player1Id = p1.id();
            player2Id = p2.id();
        } catch (Exception e) {
            // Players might already exist
        }
    }

    @Test
    void findAllTeams() {
        var teams = teamService.findAll(null, null);
        assertNotNull(teams);
    }

    @Test
    void createTeamSamePlayerTwice() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> teamService.create(new TeamRequest("Corp", 1L, 1L, null)));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("différents"));
    }

    @Test
    void teamNotFound() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> teamService.findById(99999L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
