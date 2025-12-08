package com.polypadel;

import com.polypadel.dto.*;
import com.polypadel.model.*;
import com.polypadel.repository.*;
import com.polypadel.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ServiceEdgeCasesTest {

    @Autowired private PlayerService playerService;
    @Autowired private TeamService teamService;
    @Autowired private PoolService poolService;
    @Autowired private EventService eventService;
    @Autowired private MatchService matchService;
    @Autowired private AuthService authService;

    @Test
    void deletePlayerNotFound() {
        assertThrows(ResponseStatusException.class, () -> playerService.delete(99999L));
    }

    @Test
    void updatePlayerNotFound() {
        assertThrows(ResponseStatusException.class, () -> 
            playerService.update(99999L, new PlayerRequest("A", "B", "C", "L999999", "a@b.com")));
    }

    @Test
    void deleteTeamNotFound() {
        assertThrows(ResponseStatusException.class, () -> teamService.delete(99999L));
    }

    @Test
    void updateTeamNotFound() {
        assertThrows(ResponseStatusException.class, () -> 
            teamService.update(99999L, new TeamRequest("Corp", 1L, 2L, null)));
    }

    @Test
    void deletePoolNotFound() {
        assertThrows(ResponseStatusException.class, () -> poolService.delete(99999L));
    }

    @Test
    void updatePoolNotFound() {
        assertThrows(ResponseStatusException.class, () -> 
            poolService.update(99999L, new PoolRequest("Pool", List.of(1L,2L,3L,4L,5L,6L))));
    }

    @Test
    void deleteEventNotFound() {
        assertThrows(ResponseStatusException.class, () -> eventService.delete(99999L));
    }

    @Test
    void updateEventNotFound() {
        assertThrows(ResponseStatusException.class, () -> 
            eventService.update(99999L, new EventRequest(LocalDate.now().plusDays(1), LocalTime.of(19, 0), List.of())));
    }

    @Test
    void updateMatchNotFound() {
        assertThrows(ResponseStatusException.class, () -> 
            matchService.update(99999L, new MatchUpdateRequest("TERMINE", "6-4", "4-6")));
    }

    @Test
    void createTeamPlayerNotFound() {
        assertThrows(ResponseStatusException.class, () -> 
            teamService.create(new TeamRequest("Corp", 99999L, 99998L, null)));
    }

    @Test
    void eventWithTeamNotFound() {
        EventRequest.MatchInfo m = new EventRequest.MatchInfo(1, 99999L, 99998L);
        EventRequest req = new EventRequest(LocalDate.now().plusDays(30), LocalTime.of(19, 0), List.of(m));
        assertThrows(ResponseStatusException.class, () -> eventService.create(req));
    }

    @Test
    void createPoolWithDuplicateName() {
        // First create might succeed, second should fail
        try {
            poolService.create(new PoolRequest("DuplicatePool", List.of(1L,2L,3L,4L,5L,6L)));
        } catch (Exception e) {
            // Ignore
        }
        // This should fail - duplicate name
        assertThrows(ResponseStatusException.class, () -> 
            poolService.create(new PoolRequest("DuplicatePool", List.of(1L,2L,3L,4L,5L,6L))));
    }

    @Test
    void passwordChangeWrongCurrent() {
        User user = new User("test@test.com", "hash", Role.JOUEUR);
        assertThrows(ResponseStatusException.class, () -> 
            authService.changePassword(user, new PasswordChangeRequest("wrong", "NewP@ssw0rd123!", "NewP@ssw0rd123!")));
    }

    @Test
    void eventWithSameTeamTwice() {
        EventRequest.MatchInfo m = new EventRequest.MatchInfo(1, 1L, 1L);
        EventRequest req = new EventRequest(LocalDate.now().plusDays(30), LocalTime.of(19, 0), List.of(m));
        assertThrows(ResponseStatusException.class, () -> eventService.create(req));
    }
}
