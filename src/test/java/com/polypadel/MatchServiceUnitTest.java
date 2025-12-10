package com.polypadel;

import com.polypadel.dto.MatchCreateRequest;
import com.polypadel.dto.MatchResponse;
import com.polypadel.dto.MatchUpdateRequest;
import com.polypadel.dto.TeamResponse;
import com.polypadel.model.*;
import com.polypadel.repository.EventRepository;
import com.polypadel.repository.MatchRepository;
import com.polypadel.repository.TeamRepository;
import com.polypadel.service.MatchService;
import com.polypadel.service.TeamService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MatchServiceUnitTest {

    @Mock private MatchRepository matchRepository;
    @Mock private TeamService teamService;
    @Mock private TeamRepository teamRepository;
    @Mock private EventRepository eventRepository;
    @InjectMocks private MatchService matchService;

    private Event event() {
        Event event = new Event();
        event.setId(1L);
        event.setEventDate(LocalDate.now());
        event.setEventTime(LocalTime.NOON);
        return event;
    }

    private Team team(long id) {
        Team t = new Team();
        t.setId(id);
        t.setCompany("Team" + id);
        return t;
    }

    @Test
    void createMatchHappyPath() {
        MatchCreateRequest request = new MatchCreateRequest(1L, 2L, 3L, 4);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event()));
        when(teamRepository.findById(2L)).thenReturn(Optional.of(team(2L)));
        when(teamRepository.findById(3L)).thenReturn(Optional.of(team(3L)));
        when(teamService.toResponse(any(Team.class))).thenReturn(
            new TeamResponse(2L, "Team2", java.util.List.of(), null),
            new TeamResponse(3L, "Team3", java.util.List.of(), null)
        );
        when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> {
            Match m = invocation.getArgument(0);
            m.setId(50L);
            return m;
        });

        MatchResponse response = matchService.create(request);

        assertEquals("A_VENIR", response.status());
        assertEquals(4, response.courtNumber());
        assertEquals(50L, response.id());
    }

    @Test
    void createMatchRejectsSameTeam() {
        MatchCreateRequest request = new MatchCreateRequest(1L, 2L, 2L, 1);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event()));
        when(teamRepository.findById(2L)).thenReturn(Optional.of(team(2L)));

        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
            () -> matchService.create(request)).getStatusCode());
    }

    @Test
    void updateSetsScoresAndStatus() {
        Match match = new Match();
        match.setId(5L);
        match.setEvent(event());
        match.setTeam1(team(10L));
        match.setTeam2(team(11L));
        match.setStatus(MatchStatus.A_VENIR);
        match.setCourtNumber(1);

        when(matchRepository.findById(5L)).thenReturn(Optional.of(match));
        when(matchRepository.save(eq(match))).thenReturn(match);
        when(teamService.toResponse(any(Team.class))).thenReturn(
            new TeamResponse(10L, "A", java.util.List.of(), null),
            new TeamResponse(11L, "B", java.util.List.of(), null)
        );

        MatchResponse response = matchService.update(5L,
            new MatchUpdateRequest("TERMINE", "6-4", "4-6"));

        assertEquals("TERMINE", response.status());
        assertEquals("6-4", match.getScoreTeam1());
        assertEquals("4-6", match.getScoreTeam2());
    }

    @Test
    void deleteBlocksCompletedMatches() {
        Match match = new Match();
        match.setId(9L);
        match.setStatus(MatchStatus.TERMINE);

        when(matchRepository.findById(9L)).thenReturn(Optional.of(match));

        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
            () -> matchService.delete(9L)).getStatusCode());
    }
}
