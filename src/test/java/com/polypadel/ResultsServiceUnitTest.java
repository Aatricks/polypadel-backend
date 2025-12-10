package com.polypadel;

import com.polypadel.dto.MyResultsResponse;
import com.polypadel.model.*;
import com.polypadel.repository.MatchRepository;
import com.polypadel.repository.PlayerRepository;
import com.polypadel.repository.TeamRepository;
import com.polypadel.service.ResultsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultsServiceUnitTest {

    @Mock private MatchRepository matchRepository;
    @Mock private PlayerRepository playerRepository;
    @Mock private TeamRepository teamRepository;
    @InjectMocks private ResultsService resultsService;

    @Test
    void aggregatesWinsAndLosses() {
        User user = new User();
        user.setId(1L);

        Player player = new Player();
        player.setId(10L);

        Team myTeam = new Team();
        myTeam.setId(100L);
        myTeam.setCompany("MyCorp");

        Team opponentA = new Team();
        opponentA.setId(200L);
        opponentA.setCompany("OppA");
        Player oppA1 = new Player();
        oppA1.setFirstName("Alice");
        oppA1.setLastName("Doe");
        opponentA.setPlayer1(oppA1);

        Team opponentB = new Team();
        opponentB.setId(300L);
        opponentB.setCompany("OppB");
        Player oppB1 = new Player();
        oppB1.setFirstName("Bob");
        oppB1.setLastName("Smith");
        opponentB.setPlayer1(oppB1);

        Event event = new Event();
        event.setEventDate(LocalDate.of(2025, 1, 1));
        event.setEventTime(LocalTime.of(10, 0));

        Match winMatch = new Match();
        winMatch.setId(1L);
        winMatch.setEvent(event);
        winMatch.setTeam1(myTeam);
        winMatch.setTeam2(opponentA);
        winMatch.setCourtNumber(1);
        winMatch.setScoreTeam1("6-4, 7-5");
        winMatch.setScoreTeam2("4-6, 5-7");

        Match lossMatch = new Match();
        lossMatch.setId(2L);
        lossMatch.setEvent(event);
        lossMatch.setTeam1(opponentB);
        lossMatch.setTeam2(myTeam);
        lossMatch.setCourtNumber(2);
        lossMatch.setScoreTeam1("6-3, 4-6, 7-6");
        lossMatch.setScoreTeam2("3-6, 6-4, 6-7");

        when(playerRepository.findByUserId(1L)).thenReturn(Optional.of(player));
        when(teamRepository.findByPlayerId(10L)).thenReturn(List.of(myTeam));
        when(matchRepository.findCompletedByTeamId(100L)).thenReturn(List.of(winMatch, lossMatch));

        MyResultsResponse response = resultsService.getMyResults(user);

        assertEquals(2, response.results().size());
        assertEquals(2, response.statistics().totalMatches());
        assertEquals(1, response.statistics().wins());
        assertEquals(1, response.statistics().losses());
        assertEquals(50.0, response.statistics().winRate());
        assertEquals("VICTOIRE", response.results().get(0).result());
        assertEquals("DEFAITE", response.results().get(1).result());
        assertEquals("OppA", response.results().get(0).opponents().company());
    }

    @Test
    void handlesMissingPlayerOrTeams() {
        User user = new User();
        user.setId(5L);

        when(playerRepository.findByUserId(5L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> resultsService.getMyResults(user));

        Player player = new Player();
        player.setId(50L);
        when(playerRepository.findByUserId(5L)).thenReturn(Optional.of(player));
        when(teamRepository.findByPlayerId(50L)).thenReturn(List.of());

        MyResultsResponse empty = resultsService.getMyResults(user);
        assertTrue(empty.results().isEmpty());
        assertEquals(0, empty.statistics().totalMatches());
    }
}
