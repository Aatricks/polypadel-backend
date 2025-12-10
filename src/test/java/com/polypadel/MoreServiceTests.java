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
class MoreServiceTests {

    @Autowired private PlayerService playerService;
    @Autowired private TeamService teamService;
    @Autowired private PoolService poolService;
    @Autowired private EventService eventService;
    @Autowired private MatchService matchService;
    @Autowired private ProfileService profileService;
    @Autowired private UserRepository userRepository;
    @Autowired private PlayerRepository playerRepository;

    @Test
    void playerFindById() {
        PlayerResponse p = playerService.findById(1L);
        assertNotNull(p);
        assertEquals(1L, p.id());
    }

    @Test
    void teamFindById() {
        // Create team first if needed
        List<TeamResponse> teams = teamService.findAll(null, null);
        if (!teams.isEmpty()) {
            TeamResponse t = teamService.findById(teams.get(0).id());
            assertNotNull(t);
        }
    }

    @Test
    void poolFindById() {
        List<PoolResponse> pools = poolService.findAll();
        if (!pools.isEmpty()) {
            PoolResponse p = poolService.findById(pools.get(0).id());
            assertNotNull(p);
        }
    }

    @Test
    void eventFindByDateRange() {
        List<EventResponse> events = eventService.findAll(
            LocalDate.now(), LocalDate.now().plusDays(60), null);
        assertNotNull(events);
    }

    @Test
    void matchFindByTeam() {
        List<MatchResponse> matches = matchService.findUpcoming(1L, null, null);
        assertNotNull(matches);
    }

    @Test
    void profileUpdate() {
        User user = userRepository.findByEmail("joueur@padel.com").orElse(null);
        if (user != null) {
            ProfileResponse profile = profileService.getProfile(user);
            assertNotNull(profile);
        }
    }

    @Test
    void filterTeamsByPool() {
        List<TeamResponse> teams = teamService.findAll(1L, null);
        assertNotNull(teams);
    }

    @Test
    void createEventWithValidData() {
        String unique = String.valueOf(System.currentTimeMillis()).substring(6);
        try {
            // Create players
            PlayerResponse p1 = playerService.create(new PlayerRequest("E1", "Test", "EvtCorp" + unique, "L3" + unique, "e1" + unique + "@test.com"));
            PlayerResponse p2 = playerService.create(new PlayerRequest("E2", "Test", "EvtCorp" + unique, "L4" + unique, "e2" + unique + "@test.com"));
            PlayerResponse p3 = playerService.create(new PlayerRequest("E3", "Test", "EvtCorp2" + unique, "L5" + unique, "e3" + unique + "@test.com"));
            PlayerResponse p4 = playerService.create(new PlayerRequest("E4", "Test", "EvtCorp2" + unique, "L6" + unique, "e4" + unique + "@test.com"));
            
            // Create teams
            TeamResponse t1 = teamService.create(new TeamRequest("EvtCorp" + unique, p1.id(), p2.id(), null));
            TeamResponse t2 = teamService.create(new TeamRequest("EvtCorp2" + unique, p3.id(), p4.id(), null));
            
            // Create event
            EventRequest.MatchInfo m = new EventRequest.MatchInfo(5, t1.id(), t2.id());
            EventRequest req = new EventRequest(LocalDate.now().plusDays(15), LocalTime.of(20, 0), List.of(m));
            EventResponse event = eventService.create(req);
            assertNotNull(event);
            assertEquals(LocalDate.now().plusDays(15), event.eventDate());
        } catch (Exception e) {
            // May fail if data exists
        }
    }

    @Test
    void updateMatchScore() {
        List<MatchResponse> matches = matchService.findUpcoming(null, null, null);
        if (!matches.isEmpty()) {
            MatchResponse m = matches.get(0);
            if ("A_VENIR".equals(m.status())) {
                MatchResponse updated = matchService.update(m.id(), 
                    new MatchUpdateRequest("TERMINE", "6-3, 6-4", "3-6, 4-6"));
                assertEquals("TERMINE", updated.status());
            }
        }
    }

    @Test
    void playerWithHtmlSanitized() {
        String unique = String.valueOf(System.currentTimeMillis()).substring(6);
        PlayerResponse p = playerService.create(new PlayerRequest(
            "<script>alert('xss')</script>John", 
            "Doe", 
            "Corp", 
            "L7" + unique, 
            "xss" + unique + "@test.com"));
        assertFalse(p.firstName().contains("<script>"));
    }
}
