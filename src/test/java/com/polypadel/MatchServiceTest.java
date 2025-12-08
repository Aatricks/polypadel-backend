package com.polypadel;

import com.polypadel.service.MatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MatchServiceTest {

    @Autowired
    private MatchService matchService;

    @Test
    void findUpcomingMatches() {
        var matches = matchService.findUpcoming(null, null, null);
        assertNotNull(matches);
    }

    @Test
    void matchNotFound() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> matchService.findById(99999L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void deleteMatchNotFound() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> matchService.delete(99999L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
