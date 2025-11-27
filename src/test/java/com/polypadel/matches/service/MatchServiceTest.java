package com.polypadel.matches.service;

import com.polypadel.common.exception.BusinessException;
import com.polypadel.domain.entity.Equipe;
import com.polypadel.domain.entity.Evenement;
import com.polypadel.domain.entity.Joueur;
import com.polypadel.domain.entity.Match;
import com.polypadel.domain.enums.MatchStatus;
import com.polypadel.equipes.repository.EquipeRepository;
import com.polypadel.events.repository.EventRepository;
import com.polypadel.joueurs.repository.JoueurRepository;
import com.polypadel.matches.dto.MatchCreateRequest;
import com.polypadel.matches.dto.MatchResponse;
import com.polypadel.matches.dto.MatchUpdateScoreRequest;
import com.polypadel.matches.mapper.MatchMapper;
import com.polypadel.matches.repository.MatchRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private EquipeRepository equipeRepository;
    @Mock
    private JoueurRepository joueurRepository;
    @Mock
    private MatchMapper matchMapper;

    @InjectMocks
    private MatchService matchService;

    private Equipe t1;
    private Equipe t2;
    private Evenement event;

    @BeforeEach
    public void setUp() {
        t1 = new Equipe(); t1.setId(UUID.randomUUID());
        t2 = new Equipe(); t2.setId(UUID.randomUUID());
        event = new Evenement(); event.setId(UUID.randomUUID()); event.setDateDebut(LocalDate.now());
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void create_ok() {
        MatchCreateRequest req = new MatchCreateRequest(event.getId(), t1.getId(), t2.getId(), 1, LocalTime.of(9,0));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(equipeRepository.findById(t1.getId())).thenReturn(Optional.of(t1));
        when(equipeRepository.findById(t2.getId())).thenReturn(Optional.of(t2));
        when(matchRepository.existsByEvenementIdAndPisteAndStartTime(any(), anyInt(), any())).thenReturn(false);
        when(matchRepository.existsByEvenementIdAndEquipe1IdOrEvenementIdAndEquipe2Id(any(), any(), any(), any())).thenReturn(false);
        Match saved = new Match(); saved.setId(UUID.randomUUID()); saved.setEvenement(event); saved.setEquipe1(t1); saved.setEquipe2(t2);
        when(matchRepository.save(any())).thenReturn(saved);
        when(matchMapper.toResponse(saved)).thenReturn(new MatchResponse(saved.getId(), t1.getId(), t2.getId(), null, 1, LocalTime.of(9,0), MatchStatus.A_VENIR, null, null));

        MatchResponse resp = matchService.create(req);
        assertThat(resp).isNotNull();
        verify(matchRepository).save(any());
    }

    @Test
    public void create_same_teams_throws() {
        MatchCreateRequest req = new MatchCreateRequest(event.getId(), t1.getId(), t1.getId(), 1, LocalTime.of(9,0));
        assertThatThrownBy(() -> matchService.create(req)).isInstanceOf(BusinessException.class);
    }

    @Test
    public void upcomingForCurrentUser_returns_list() {
        UUID userId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(userId.toString());
        SecurityContextHolder.getContext().setAuthentication(auth);
        Joueur joueur = new Joueur(); joueur.setId(UUID.randomUUID());
        when(joueurRepository.findByUtilisateurId(userId)).thenReturn(Optional.of(joueur));
        when(equipeRepository.findIdsByPlayer(joueur.getId())).thenReturn(List.of(t1.getId()));
        Match m = new Match(); m.setId(UUID.randomUUID()); m.setEquipe1(t1); m.setEquipe2(t2); m.setStatut(MatchStatus.A_VENIR);
        when(matchRepository.findUpcomingForTeams(anyList(), anyList())).thenReturn(List.of(m));
        when(matchMapper.toResponse(m)).thenReturn(new MatchResponse(m.getId(), t1.getId(), t2.getId(), null, 1, LocalTime.of(9,0), MatchStatus.A_VENIR, null, null));

        var list = matchService.upcomingForCurrentUser();
        assertThat(list).hasSize(1);
    }

    @Test
    public void create_slotTaken_throws() {
        MatchCreateRequest req = new MatchCreateRequest(event.getId(), t1.getId(), t2.getId(), 1, LocalTime.of(9,0));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(equipeRepository.findById(t1.getId())).thenReturn(Optional.of(t1));
        when(equipeRepository.findById(t2.getId())).thenReturn(Optional.of(t2));
        when(matchRepository.existsByEvenementIdAndPisteAndStartTime(any(), anyInt(), any())).thenReturn(true);

        assertThatThrownBy(() -> matchService.create(req)).isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo("MATCH_SLOT_TAKEN"));
    }

    @Test
    public void create_teamAlreadyInEvent_throws_for_equipe1() {
        MatchCreateRequest req = new MatchCreateRequest(event.getId(), t1.getId(), t2.getId(), 1, LocalTime.of(9,0));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(equipeRepository.findById(t1.getId())).thenReturn(Optional.of(t1));
        when(equipeRepository.findById(t2.getId())).thenReturn(Optional.of(t2));
        when(matchRepository.existsByEvenementIdAndPisteAndStartTime(any(), anyInt(), any())).thenReturn(false);
        when(matchRepository.existsByEvenementIdAndEquipe1IdOrEvenementIdAndEquipe2Id(any(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> matchService.create(req)).isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo("TEAM_ALREADY_IN_EVENT"));
    }

    @Test
    public void create_teamAlreadyInEvent_throws_for_equipe2() {
        MatchCreateRequest req = new MatchCreateRequest(event.getId(), t1.getId(), t2.getId(), 1, LocalTime.of(9,0));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(equipeRepository.findById(t1.getId())).thenReturn(Optional.of(t1));
        when(equipeRepository.findById(t2.getId())).thenReturn(Optional.of(t2));
        when(matchRepository.existsByEvenementIdAndPisteAndStartTime(any(), anyInt(), any())).thenReturn(false);
        // Return false for first check and true for second - simulate t2 already in event
        when(matchRepository.existsByEvenementIdAndEquipe1IdOrEvenementIdAndEquipe2Id(any(), eq(t1.getId()), any(), eq(t1.getId()))).thenReturn(false);
        when(matchRepository.existsByEvenementIdAndEquipe1IdOrEvenementIdAndEquipe2Id(any(), eq(t2.getId()), any(), eq(t2.getId()))).thenReturn(true);

        assertThatThrownBy(() -> matchService.create(req)).isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo("TEAM_ALREADY_IN_EVENT"));
    }

    @Test
    public void updateScore_termine_valid_scores_updates() {
        UUID matchId = UUID.randomUUID();
        Match m = new Match(); m.setId(matchId); m.setStatut(MatchStatus.A_VENIR);
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(m));
        when(matchRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(matchMapper.toResponse(any())).thenReturn(new MatchResponse(matchId, t1.getId(), t2.getId(), null, 1, LocalTime.of(9,0), MatchStatus.TERMINE, "6-4,7-5", "4-6,5-7"));

        MatchUpdateScoreRequest req = new MatchUpdateScoreRequest("6-4,7-5", "4-6,5-7", MatchStatus.TERMINE);
        var resp = matchService.updateScore(matchId, req);
        assertThat(resp.statut()).isEqualTo(MatchStatus.TERMINE);
        verify(matchRepository).save(any());
    }

    @Test
    public void updateScore_termine_invalid_scores_throws() {
        UUID matchId = UUID.randomUUID();
        Match m = new Match(); m.setId(matchId); m.setStatut(MatchStatus.A_VENIR);
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(m));

        MatchUpdateScoreRequest req = new MatchUpdateScoreRequest("bad", "also-bad", MatchStatus.TERMINE);
        assertThatThrownBy(() -> matchService.updateScore(matchId, req)).isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo("INVALID_SCORE"));
    }

    @Test
    public void updateScore_partial_scores_updates_only_score1() {
        UUID matchId = UUID.randomUUID();
        Match m = new Match(); m.setId(matchId); m.setStatut(MatchStatus.A_VENIR); m.setScore1("0-0"); m.setScore2("0-0");
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(m));
        when(matchRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(matchMapper.toResponse(any())).thenReturn(new MatchResponse(matchId, t1.getId(), t2.getId(), null, 1, LocalTime.of(9,0), MatchStatus.A_VENIR, "6-4", "0-0"));

        MatchUpdateScoreRequest req = new MatchUpdateScoreRequest("6-4", null, null);
        var resp = matchService.updateScore(matchId, req);
        verify(matchRepository).save(any());
        assertThat(resp.score1()).isEqualTo("6-4");
        assertThat(resp.score2()).isEqualTo("0-0");
    }

    @Test
    public void updateScore_when_status_null_does_not_change_status() {
        UUID matchId = UUID.randomUUID();
        Match m = new Match(); m.setId(matchId); m.setStatut(MatchStatus.A_VENIR);
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(m));
        when(matchRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(matchMapper.toResponse(any())).thenReturn(new MatchResponse(matchId, t1.getId(), t2.getId(), null, 1, LocalTime.of(9,0), MatchStatus.A_VENIR, "6-4", null));

        MatchUpdateScoreRequest req = new MatchUpdateScoreRequest("6-4", null, null);
        var resp = matchService.updateScore(matchId, req);
        assertThat(resp.statut()).isEqualTo(MatchStatus.A_VENIR);
    }

    @Test
    public void upcomingForCurrentUser_returns_empty_when_no_teams() {
        UUID userId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(userId.toString());
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(joueurRepository.findByUtilisateurId(userId)).thenReturn(Optional.of(new Joueur()));
        when(equipeRepository.findIdsByPlayer(any())).thenReturn(List.of());

        var list = matchService.upcomingForCurrentUser();
        assertThat(list).isEmpty();
    }

    @Test
    public void finishedForCurrentUser_returns_list() {
        UUID userId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(userId.toString());
        SecurityContextHolder.getContext().setAuthentication(auth);
        Joueur joueur = new Joueur(); joueur.setId(UUID.randomUUID());
        when(joueurRepository.findByUtilisateurId(userId)).thenReturn(Optional.of(joueur));
        when(equipeRepository.findIdsByPlayer(joueur.getId())).thenReturn(List.of(t1.getId()));
        Match m = new Match(); m.setId(UUID.randomUUID()); m.setEquipe1(t1); m.setEquipe2(t2); m.setStatut(MatchStatus.TERMINE);
        when(matchRepository.findUpcomingForTeams(anyList(), anyList())).thenReturn(List.of(m));
        when(matchMapper.toResponse(m)).thenReturn(new MatchResponse(m.getId(), t1.getId(), t2.getId(), null, 1, LocalTime.of(9,0), MatchStatus.TERMINE, null, null));

        var list = matchService.finishedForCurrentUser();
        assertThat(list).hasSize(1);
    }

    @Test
    public void listByEvent_returns_matches_for_event() {
        Match m = new Match(); m.setId(UUID.randomUUID()); m.setEquipe1(t1); m.setEquipe2(t2);
        when(matchRepository.findByEvenementIdOrderByStartTimeAsc(event.getId())).thenReturn(List.of(m));
        when(matchMapper.toResponse(m)).thenReturn(new MatchResponse(m.getId(), t1.getId(), t2.getId(), null, 1, LocalTime.of(9,0), MatchStatus.A_VENIR, null, null));

        var list = matchService.listByEvent(event.getId());
        assertThat(list).hasSize(1);
    }
}
