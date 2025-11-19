package com.polypadel.equipes.service;

import com.polypadel.common.exception.BusinessException;
import com.polypadel.domain.entity.Equipe;
import com.polypadel.domain.entity.Joueur;
import com.polypadel.domain.entity.Poule;
import com.polypadel.equipes.dto.TeamCreateRequest;
import com.polypadel.equipes.dto.TeamResponse;
import com.polypadel.equipes.dto.TeamUpdateRequest;
import com.polypadel.equipes.mapper.EquipeMapper;
import com.polypadel.equipes.repository.EquipeRepository;
import com.polypadel.joueurs.repository.JoueurRepository;
import com.polypadel.matches.repository.MatchRepository;
import com.polypadel.poules.repository.PouleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EquipeServiceTest {

    @Mock
    private EquipeRepository equipeRepository;
    @Mock
    private JoueurRepository joueurRepository;
    @Mock
    private PouleRepository pouleRepository;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private EquipeMapper equipeMapper;

    @InjectMocks
    private EquipeService equipeService;

    private Joueur joueurA;
    private Joueur joueurB;
    private Poule poule;

    @BeforeEach
    public void setup() {
        joueurA = new Joueur();
        joueurA.setId(UUID.randomUUID());
        joueurA.setEntreprise("Ent1");
        joueurB = new Joueur();
        joueurB.setId(UUID.randomUUID());
        joueurB.setEntreprise("Ent1");
        poule = new Poule();
        poule.setId(UUID.randomUUID());
    }

    @Test
    public void create_ok() {
        TeamCreateRequest req = new TeamCreateRequest("Ent1", joueurA.getId(), joueurB.getId(), null);
        when(joueurRepository.findById(joueurA.getId())).thenReturn(Optional.of(joueurA));
        when(joueurRepository.findById(joueurB.getId())).thenReturn(Optional.of(joueurB));
        when(equipeRepository.existsByJoueur1IdOrJoueur2Id(any(), any())).thenReturn(false);
        Equipe saved = new Equipe(); saved.setId(UUID.randomUUID()); saved.setEntreprise("Ent1");
        when(equipeRepository.save(any())).thenReturn(saved);
        when(equipeMapper.toResponse(saved)).thenReturn(new TeamResponse(saved.getId(), "Ent1", null, joueurA.getId(), joueurB.getId()));

        TeamResponse resp = equipeService.create(req);
        assertThat(resp).isNotNull();
        assertThat(resp.entreprise()).isEqualTo("Ent1");
        verify(equipeRepository).save(any());
    }

    @Test
    public void create_with_poule_ok() {
        TeamCreateRequest req = new TeamCreateRequest("Ent1", joueurA.getId(), joueurB.getId(), poule.getId());
        when(joueurRepository.findById(joueurA.getId())).thenReturn(Optional.of(joueurA));
        when(joueurRepository.findById(joueurB.getId())).thenReturn(Optional.of(joueurB));
        when(equipeRepository.existsByJoueur1IdOrJoueur2Id(any(), any())).thenReturn(false);
        when(pouleRepository.findById(poule.getId())).thenReturn(Optional.of(poule));
        when(equipeRepository.countByPouleId(poule.getId())).thenReturn(0);
        Equipe saved = new Equipe(); saved.setId(UUID.randomUUID()); saved.setEntreprise("Ent1"); saved.setPoule(poule);
        when(equipeRepository.save(any())).thenReturn(saved);
        when(equipeMapper.toResponse(saved)).thenReturn(new TeamResponse(saved.getId(), "Ent1", poule.getId(), joueurA.getId(), joueurB.getId()));

        TeamResponse resp = equipeService.create(req);
        assertThat(resp.pouleId()).isEqualTo(poule.getId());
    }

    @Test
    public void create_fails_when_poule_full() {
        TeamCreateRequest req = new TeamCreateRequest("Ent1", joueurA.getId(), joueurB.getId(), poule.getId());
        when(joueurRepository.findById(joueurA.getId())).thenReturn(Optional.of(joueurA));
        when(joueurRepository.findById(joueurB.getId())).thenReturn(Optional.of(joueurB));
        when(equipeRepository.existsByJoueur1IdOrJoueur2Id(any(), any())).thenReturn(false);
        when(pouleRepository.findById(poule.getId())).thenReturn(Optional.of(poule));
        when(equipeRepository.countByPouleId(any())).thenReturn(6);

        System.out.println("DEBUG req.pouleId=" + req.pouleId());
        System.out.println("DEBUG poule.id=" + poule.getId());
        assertThatThrownBy(() -> equipeService.create(req)).isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo("POULE_SIZE_VIOLATION"));
        verify(pouleRepository).findById(poule.getId());
        verify(equipeRepository).countByPouleId(any());
    }

    @Test
    public void create_fails_when_players_have_different_enterprise() {
        joueurB.setEntreprise("Other");
        TeamCreateRequest req = new TeamCreateRequest("Ent1", joueurA.getId(), joueurB.getId(), null);
        when(joueurRepository.findById(joueurA.getId())).thenReturn(Optional.of(joueurA));
        when(joueurRepository.findById(joueurB.getId())).thenReturn(Optional.of(joueurB));

        assertThatThrownBy(() -> equipeService.create(req))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo("TEAM_DIFFERENT_ENTREPRISE"));
    }

    @Test
    public void create_fails_when_player_already_in_team() {
        TeamCreateRequest req = new TeamCreateRequest("Ent1", joueurA.getId(), joueurB.getId(), null);
        when(joueurRepository.findById(joueurA.getId())).thenReturn(Optional.of(joueurA));
        when(joueurRepository.findById(joueurB.getId())).thenReturn(Optional.of(joueurB));
        when(equipeRepository.existsByJoueur1IdOrJoueur2Id(joueurA.getId(), joueurA.getId())).thenReturn(true);

        assertThatThrownBy(() -> equipeService.create(req)).isInstanceOf(BusinessException.class);
    }

    @Test
    public void create_fails_when_playerB_already_in_team() {
        TeamCreateRequest req = new TeamCreateRequest("Ent1", joueurA.getId(), joueurB.getId(), null);
        when(joueurRepository.findById(joueurA.getId())).thenReturn(Optional.of(joueurA));
        when(joueurRepository.findById(joueurB.getId())).thenReturn(Optional.of(joueurB));
        // Return true only when the checked id is joueurB to simulate second player already in a team
        when(equipeRepository.existsByJoueur1IdOrJoueur2Id(any(), any())).thenAnswer(invocation -> {
            java.util.UUID arg = invocation.getArgument(0);
            return arg.equals(joueurB.getId());
        });

        assertThatThrownBy(() -> equipeService.create(req)).isInstanceOf(BusinessException.class);
    }

    @Test
    public void assignToPoule_success() {
        UUID teamId = UUID.randomUUID();
        Equipe e = new Equipe(); e.setId(teamId); e.setEntreprise("Ent1"); e.setJoueur1(joueurA); e.setJoueur2(joueurB);
        when(equipeRepository.findById(teamId)).thenReturn(Optional.of(e));
        when(pouleRepository.findById(poule.getId())).thenReturn(Optional.of(poule));
        when(equipeRepository.countByPouleId(poule.getId())).thenReturn(0);
        when(equipeRepository.save(any())).thenReturn(e);
        when(equipeMapper.toResponse(e)).thenReturn(new TeamResponse(e.getId(), e.getEntreprise(), poule.getId(), joueurA.getId(), joueurB.getId()));

        var res = equipeService.assignToPoule(teamId, poule.getId());
        assertThat(res.pouleId()).isEqualTo(poule.getId());
    }

    @Test
    public void assignToPoule_same_poule_allows_even_if_poule_full() {
        UUID teamId = UUID.randomUUID();
        Equipe e = new Equipe(); e.setId(teamId); e.setEntreprise("Ent1"); e.setJoueur1(joueurA); e.setJoueur2(joueurB); e.setPoule(poule);
        when(equipeRepository.findById(teamId)).thenReturn(Optional.of(e));
        when(pouleRepository.findById(poule.getId())).thenReturn(Optional.of(poule));
        // No need to stub countByPouleId since the team is already in the same poule and the
        // assignToPoule code will skip the poule-size check in this case.
        when(equipeRepository.save(any())).thenReturn(e);
        when(equipeMapper.toResponse(e)).thenReturn(new TeamResponse(e.getId(), e.getEntreprise(), poule.getId(), joueurA.getId(), joueurB.getId()));

        var res = equipeService.assignToPoule(teamId, poule.getId());
        assertThat(res.pouleId()).isEqualTo(poule.getId());
    }

    @Test
    public void assignToPoule_fails_when_poule_full() {
        UUID teamId = UUID.randomUUID();
        Equipe e = new Equipe(); e.setId(teamId); e.setEntreprise("Ent1"); e.setJoueur1(joueurA); e.setJoueur2(joueurB);
        when(equipeRepository.findById(teamId)).thenReturn(Optional.of(e));
        when(pouleRepository.findById(poule.getId())).thenReturn(Optional.of(poule));
        // Stub countByPouleId to simulate a full poule
        when(equipeRepository.countByPouleId(poule.getId())).thenReturn(6);

        assertThatThrownBy(() -> equipeService.assignToPoule(teamId, poule.getId())).isInstanceOf(BusinessException.class);
    }

    @Test
    public void update_allows_assign_same_poule_even_if_poule_full() {
        UUID id = UUID.randomUUID();
        Poule samePoule = new Poule(); samePoule.setId(UUID.randomUUID());
        Equipe e = new Equipe(); e.setId(id); e.setEntreprise("Ent1"); e.setJoueur1(joueurA); e.setJoueur2(joueurB); e.setPoule(samePoule);
        when(equipeRepository.findById(id)).thenReturn(Optional.of(e));
        when(matchRepository.existsByStatutAndEquipe1IdOrStatutAndEquipe2Id(any(), any(), any(), any())).thenReturn(false);
        when(pouleRepository.findById(samePoule.getId())).thenReturn(Optional.of(samePoule));
        // No need to stub countByPouleId since it should not be called when assigning to same poule
        when(equipeRepository.save(any())).thenReturn(e);
        when(equipeMapper.toResponse(any())).thenReturn(new TeamResponse(id, "Ent1", samePoule.getId(), joueurA.getId(), joueurB.getId()));

        TeamUpdateRequest req = new TeamUpdateRequest(null, null, null, samePoule.getId());
        var resp = equipeService.update(id, req);
        assertThat(resp.pouleId()).isEqualTo(samePoule.getId());
    }

    @Test
    public void create_throws_when_joueur_not_found() {
        TeamCreateRequest req = new TeamCreateRequest("Ent1", joueurA.getId(), joueurB.getId(), null);
        when(joueurRepository.findById(joueurA.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> equipeService.create(req)).isInstanceOf(Exception.class);
    }

    @Test
    public void update_throws_when_joueur_not_found() {
        UUID id = UUID.randomUUID();
        Equipe e = new Equipe(); e.setId(id); e.setEntreprise("Ent1"); e.setJoueur1(joueurA); e.setJoueur2(joueurB);
        when(equipeRepository.findById(id)).thenReturn(Optional.of(e));
        when(matchRepository.existsByStatutAndEquipe1IdOrStatutAndEquipe2Id(any(), any(), any(), any())).thenReturn(false);
        UUID missing = UUID.randomUUID();
        TeamUpdateRequest req = new TeamUpdateRequest(null, missing, null, null);
        when(joueurRepository.findById(missing)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> equipeService.update(id, req)).isInstanceOf(Exception.class);
    }

    @Test
    public void update_validates_enterprise_consistency_null_enterprise_does_not_throw() {
        UUID id = UUID.randomUUID();
        Equipe e = new Equipe(); e.setId(id); e.setEntreprise(null); e.setJoueur1(joueurA); e.setJoueur2(joueurB);
        when(equipeRepository.findById(id)).thenReturn(Optional.of(e));
        when(matchRepository.existsByStatutAndEquipe1IdOrStatutAndEquipe2Id(any(), any(), any(), any())).thenReturn(false);
        when(equipeRepository.save(any())).thenReturn(e);
        when(equipeMapper.toResponse(any())).thenReturn(new TeamResponse(id, null, null, joueurA.getId(), joueurB.getId()));

        TeamUpdateRequest req = new TeamUpdateRequest(null, null, null, null);
        var resp = equipeService.update(id, req);
        assertThat(resp.entreprise()).isNull();
    }

    @Test
    public void ensureNotLocked_prevents_update_or_delete() {
        UUID id = UUID.randomUUID();
        Equipe e = new Equipe(); e.setId(id); e.setEntreprise("Ent1"); e.setJoueur1(joueurA); e.setJoueur2(joueurB);
        // equipeRepository.findById is required for update since update loads the team first
        when(equipeRepository.findById(id)).thenReturn(Optional.of(e));
        when(matchRepository.existsByStatutAndEquipe1IdOrStatutAndEquipe2Id(any(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> equipeService.update(id, new TeamUpdateRequest(null, null, null, null))).isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo("TEAM_LOCKED"));

        assertThatThrownBy(() -> equipeService.delete(id)).isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo("TEAM_LOCKED"));
    }

    @Test
    public void delete_ok_when_not_locked() {
        UUID id = UUID.randomUUID();
        Equipe e = new Equipe(); e.setId(id); e.setEntreprise("Ent1"); e.setJoueur1(joueurA); e.setJoueur2(joueurB);
        // Not required to stub findById for delete (ensureNotLocked checks matches only),
        // leaving it out keeps the test minimal and avoids unnecessary stubbing
        when(matchRepository.existsByStatutAndEquipe1IdOrStatutAndEquipe2Id(any(), any(), any(), any())).thenReturn(false);
        equipeService.delete(id);
        verify(equipeRepository).deleteById(id);
    }

    @Test
    public void update_fails_when_enterprise_inconsistent() {
        UUID id = UUID.randomUUID();
        Joueur j3 = new Joueur(); j3.setId(UUID.randomUUID()); j3.setEntreprise("OtherCorp");
        Equipe e = new Equipe(); e.setId(id); e.setEntreprise("Ent1"); e.setJoueur1(joueurA); e.setJoueur2(joueurB);
        when(equipeRepository.findById(any())).thenReturn(Optional.of(e));
        when(matchRepository.existsByStatutAndEquipe1IdOrStatutAndEquipe2Id(any(), any(), any(), any())).thenReturn(false);

        TeamUpdateRequest req = new TeamUpdateRequest("OtherCorp", null, null, null);
        assertThatThrownBy(() -> equipeService.update(id, req)).isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo("TEAM_DIFFERENT_ENTREPRISE"));
    }

    @Test
    public void removeFromPoule_success() {
        UUID id = UUID.randomUUID();
        Equipe e = new Equipe(); e.setId(id); e.setEntreprise("Ent1"); e.setJoueur1(joueurA); e.setJoueur2(joueurB);
        when(equipeRepository.findById(org.mockito.ArgumentMatchers.any(java.util.UUID.class))).thenReturn(Optional.of(e));
        when(matchRepository.existsByStatutAndEquipe1IdOrStatutAndEquipe2Id(any(), any(), any(), any())).thenReturn(false);
        when(equipeRepository.save(any())).thenReturn(e);
        when(equipeMapper.toResponse(e)).thenReturn(new TeamResponse(e.getId(), e.getEntreprise(), null, joueurA.getId(), joueurB.getId()));

        var resp = equipeService.removeFromPoule(id);
        assertThat(resp.pouleId()).isNull();
    }

    @Test
    public void update_fails_when_poule_full() {
        UUID id = UUID.randomUUID();
        Equipe e = new Equipe(); e.setId(id); e.setEntreprise("Ent1"); e.setJoueur1(joueurA); e.setJoueur2(joueurB);
        when(equipeRepository.findById(id)).thenReturn(Optional.of(e));
        when(matchRepository.existsByStatutAndEquipe1IdOrStatutAndEquipe2Id(any(), any(), any(), any())).thenReturn(false);
        Poule newPoule = new Poule(); newPoule.setId(UUID.randomUUID());
        when(pouleRepository.findById(newPoule.getId())).thenReturn(Optional.of(newPoule));
        when(equipeRepository.countByPouleId(newPoule.getId())).thenReturn(6);

        TeamUpdateRequest req = new TeamUpdateRequest(null, null, null, newPoule.getId());
        assertThatThrownBy(() -> equipeService.update(id, req)).isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo("POULE_SIZE_VIOLATION"));
    }

    @Test
    public void update_validates_enterprise_consistency() {
        UUID id = UUID.randomUUID();
        Equipe e = new Equipe(); e.setId(id); e.setEntreprise("Ent1"); e.setJoueur1(joueurA); e.setJoueur2(joueurB);
        when(equipeRepository.findById(id)).thenReturn(Optional.of(e));
        when(matchRepository.existsByStatutAndEquipe1IdOrStatutAndEquipe2Id(any(), any(), any(), any())).thenReturn(false);
        when(equipeRepository.save(any())).thenReturn(e);
        when(equipeMapper.toResponse(e)).thenReturn(new TeamResponse(e.getId(), e.getEntreprise(), null, joueurA.getId(), joueurB.getId()));

        TeamUpdateRequest req = new TeamUpdateRequest(null, null, null, null);
        var resp = equipeService.update(id, req);
        assertThat(resp.entreprise()).isEqualTo("Ent1");
    }

    @Test
    public void get_returns_team() {
        UUID id = UUID.randomUUID();
        Equipe e = new Equipe(); e.setId(id); e.setEntreprise("Ent1");
        when(equipeRepository.findById(id)).thenReturn(Optional.of(e));
        when(equipeMapper.toResponse(e)).thenReturn(new TeamResponse(id, "Ent1", null, joueurA.getId(), joueurB.getId()));
        var resp = equipeService.get(id);
        assertThat(resp.id()).isEqualTo(id);
    }

    @Test
    public void list_returns_page() {
        var pg = org.springframework.data.domain.PageRequest.of(0, 10);
        Equipe e = new Equipe(); e.setId(UUID.randomUUID()); e.setEntreprise("Ent1");
        when(equipeRepository.findAll(pg)).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(e)));
        when(equipeMapper.toResponse(e)).thenReturn(new TeamResponse(e.getId(), "Ent1", null, joueurA.getId(), joueurB.getId()));
        var page = equipeService.list(pg);
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    public void update_changes_joueur1_and_entreprise() {
        UUID id = UUID.randomUUID();
        Equipe e = new Equipe(); e.setId(id); e.setEntreprise("Ent1"); e.setJoueur1(joueurA); e.setJoueur2(joueurB);
        when(equipeRepository.findById(id)).thenReturn(Optional.of(e));
        when(matchRepository.existsByStatutAndEquipe1IdOrStatutAndEquipe2Id(any(), any(), any(), any())).thenReturn(false);
        Joueur j3 = new Joueur(); j3.setId(UUID.randomUUID()); j3.setEntreprise("NewEnt");
        // Make sure existing joueur2 is in the same entreprise after the update
        joueurB.setEntreprise("NewEnt");
        when(joueurRepository.findById(j3.getId())).thenReturn(Optional.of(j3));
        when(equipeRepository.save(any())).thenReturn(e);
        when(equipeMapper.toResponse(e)).thenReturn(new TeamResponse(id, "NewEnt", null, j3.getId(), joueurB.getId()));

        TeamUpdateRequest req = new TeamUpdateRequest("NewEnt", j3.getId(), null, null);
        var resp = equipeService.update(id, req);
        assertThat(resp.joueur1Id()).isEqualTo(j3.getId());
        assertThat(resp.entreprise()).isEqualTo("NewEnt");
    }

    @Test
    public void update_changes_joueur2() {
        UUID id = UUID.randomUUID();
        Equipe e = new Equipe(); e.setId(id); e.setEntreprise("Ent1"); e.setJoueur1(joueurA); e.setJoueur2(joueurB);
        when(equipeRepository.findById(id)).thenReturn(Optional.of(e));
        when(matchRepository.existsByStatutAndEquipe1IdOrStatutAndEquipe2Id(any(), any(), any(), any())).thenReturn(false);
        Joueur j4 = new Joueur(); j4.setId(UUID.randomUUID()); j4.setEntreprise("Ent1");
        when(joueurRepository.findById(j4.getId())).thenReturn(Optional.of(j4));
        when(equipeRepository.save(any())).thenReturn(e);
        when(equipeMapper.toResponse(e)).thenReturn(new TeamResponse(id, "Ent1", null, joueurA.getId(), j4.getId()));

        TeamUpdateRequest req = new TeamUpdateRequest(null, null, j4.getId(), null);
        var resp = equipeService.update(id, req);
        assertThat(resp.joueur2Id()).isEqualTo(j4.getId());
    }
}
