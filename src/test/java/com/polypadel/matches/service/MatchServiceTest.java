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
import com.polypadel.matches.dto.MatchUpdateRequest; // <--- Nouveau DTO
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
import java.time.LocalDateTime;
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
    // (Note: Si vous n'utilisez pas de mapper dans le service, supprimez ce mock)
    // @Mock private MatchMapper matchMapper; 

    @InjectMocks
    private MatchService matchService;

    private Equipe t1;
    private Equipe t2;
    private Evenement event;

    @BeforeEach
    public void setUp() {
        t1 = new Equipe(); t1.setId(UUID.randomUUID());
        t2 = new Equipe(); t2.setId(UUID.randomUUID());
        event = new Evenement(); 
        event.setId(UUID.randomUUID()); 
        event.setEventDate(LocalDate.now()); // <--- Nouveau champ
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void create_ok() {
        // Nouveau constructeur DTO (LocalDateTime)
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0));
        MatchCreateRequest req = new MatchCreateRequest(event.getId(), t1.getId(), t2.getId(), 1, start);
        
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(equipeRepository.findById(t1.getId())).thenReturn(Optional.of(t1));
        when(equipeRepository.findById(t2.getId())).thenReturn(Optional.of(t2));
        
        // Mock des vérifications de disponibilité
        when(matchRepository.existsByEvenementIdAndPisteAndStartTime(any(), anyInt(), any())).thenReturn(false);
        when(matchRepository.existsByEvenementIdAndEquipe1IdOrEvenementIdAndEquipe2Id(any(), any(), any(), any())).thenReturn(false);
        
        Match saved = new Match(); 
        saved.setId(UUID.randomUUID()); 
        saved.setEvenement(event); 
        saved.setEquipe1(t1); 
        saved.setEquipe2(t2);
        saved.setStartTime(LocalTime.of(9, 0));
        
        when(matchRepository.save(any())).thenReturn(saved);

        MatchResponse resp = matchService.create(req);
        
        assertThat(resp).isNotNull();
        verify(matchRepository).save(any());
    }

    @Test
    public void create_same_teams_throws() {
        LocalDateTime start = LocalDateTime.now();
        MatchCreateRequest req = new MatchCreateRequest(event.getId(), t1.getId(), t1.getId(), 1, start);
        assertThatThrownBy(() -> matchService.create(req)).isInstanceOf(BusinessException.class);
    }

    @Test
    public void searchMatches_upcoming_returns_list() {
        // Test de la nouvelle méthode searchMatches
        UUID userId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(userId.toString());
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        Joueur joueur = new Joueur(); joueur.setId(UUID.randomUUID());
        when(joueurRepository.findByUtilisateurId(userId)).thenReturn(Optional.of(joueur));
        when(equipeRepository.findIdsByPlayer(joueur.getId())).thenReturn(List.of(t1.getId()));
        
        Match m = new Match(); 
        m.setId(UUID.randomUUID()); 
        m.setEquipe1(t1); 
        m.setEquipe2(t2); 
        m.setStatut(MatchStatus.A_VENIR);
        m.setEvenement(event);
        m.setStartTime(LocalTime.of(9, 0));
        
        // Mock de la nouvelle méthode de repo
        when(matchRepository.findByEquipe1IdInOrEquipe2IdIn(anyList(), anyList())).thenReturn(List.of(m));

        // Appel avec filters: upcoming=true, myMatches=true
        var list = matchService.searchMatches(true, true, null, null);
        
        assertThat(list).hasSize(1);
    }

    @Test
    public void update_termine_valid_scores_updates() {
        UUID matchId = UUID.randomUUID();
        Match m = new Match(); 
        m.setId(matchId); 
        m.setStatut(MatchStatus.A_VENIR);
        
        // Setup pour toResponse
        m.setEvenement(event); m.setEquipe1(t1); m.setEquipe2(t2); m.setStartTime(LocalTime.of(9,0));

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(m));
        when(matchRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Nouveau DTO Update (score, score, statut, piste)
        MatchUpdateRequest req = new MatchUpdateRequest("6-4, 7-5", "4-6, 5-7", MatchStatus.TERMINE, null);
        
        var resp = matchService.update(matchId, req);
        
        assertThat(resp.statut).isEqualTo(MatchStatus.TERMINE);
        verify(matchRepository).save(any());
    }

    @Test
    public void update_termine_invalid_scores_throws() {
        UUID matchId = UUID.randomUUID();
        Match m = new Match(); m.setId(matchId); m.setStatut(MatchStatus.A_VENIR);
        
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(m));

        MatchUpdateRequest req = new MatchUpdateRequest("bad", "bad", MatchStatus.TERMINE, null);
        
        // L'erreur attendue est INVALID_SCORE_FORMAT ou INVALID_SCORE selon votre validateur
        assertThatThrownBy(() -> matchService.update(matchId, req))
            .isInstanceOf(BusinessException.class);
    }
}