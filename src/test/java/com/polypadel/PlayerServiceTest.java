package com.polypadel;

import com.polypadel.dto.PlayerRequest;
import com.polypadel.dto.PlayerResponse;
import com.polypadel.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PlayerServiceTest {

    @Autowired
    private PlayerService playerService;

    @Test
    void findAllPlayers() {
        var players = playerService.findAll();
        assertFalse(players.isEmpty());
    }

    @Test
    void createPlayer() {
        String unique = String.valueOf(System.currentTimeMillis()).substring(7);
        PlayerRequest request = new PlayerRequest("New", "Player", "New Corp", "L" + unique, "new" + unique + "@test.com");
        PlayerResponse response = playerService.create(request);
        assertEquals("New", response.firstName());
        assertTrue(response.licenseNumber().startsWith("L"));
    }

    @Test
    void createDuplicateLicense() {
        PlayerRequest request = new PlayerRequest("Dup", "Player", "Corp", "L123456", "dup@test.com");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> playerService.create(request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void findPlayerNotFound() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> playerService.findById(99999L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
