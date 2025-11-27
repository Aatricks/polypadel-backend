package com.polypadel.poules.service;

import com.polypadel.common.exception.BusinessException;
import com.polypadel.domain.entity.Poule;
import com.polypadel.equipes.dto.TeamResponse;
import com.polypadel.equipes.repository.EquipeRepository;
import com.polypadel.equipes.service.EquipeService;
import com.polypadel.poules.dto.PouleCreateRequest;
import com.polypadel.poules.dto.PouleResponse;
import com.polypadel.poules.dto.PouleUpdateRequest;
import com.polypadel.poules.repository.PouleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class PouleServiceTest {

    private PouleRepository pouleRepository;
    private EquipeRepository equipeRepository;
    private EquipeService equipeService;
    private PouleService service;

    @BeforeEach
    void setUp() {
        pouleRepository = Mockito.mock(PouleRepository.class);
        equipeRepository = Mockito.mock(EquipeRepository.class);
        equipeService = Mockito.mock(EquipeService.class);
        service = new PouleService(pouleRepository, equipeRepository, equipeService);
    }

    @Test
    void create_returns_trimmed_name_and_counts_teams() {
        Poule p = new Poule();
        p.setId(UUID.randomUUID());
        p.setNom("MyPoule");
        Mockito.when(pouleRepository.save(any(Poule.class))).thenReturn(p);
        Mockito.when(equipeRepository.countByPouleId(eq(p.getId()))).thenReturn(3);

        PouleCreateRequest req = new PouleCreateRequest("  MyPoule  ");
        PouleResponse resp = service.create(req);
        assertEquals("MyPoule", resp.nom());
        assertEquals(3, resp.teamCount());
    }

    @Test
    void update_trims_name() {
        UUID id = UUID.randomUUID();
        Poule existing = new Poule();
        existing.setId(id);
        existing.setNom("Old");
        Mockito.when(pouleRepository.findById(eq(id))).thenReturn(Optional.of(existing));
        Mockito.when(pouleRepository.save(existing)).thenReturn(existing);
        Mockito.when(equipeRepository.countByPouleId(eq(id))).thenReturn(0);

        PouleUpdateRequest req = new PouleUpdateRequest(" Updated ");
        PouleResponse resp = service.update(id, req);
        assertEquals("Updated", resp.nom());
    }

    @Test
    void delete_when_not_empty_throws() {
        UUID id = UUID.randomUUID();
        Mockito.when(equipeRepository.existsByPouleId(eq(id))).thenReturn(true);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(id));
        assertEquals("POULE_NOT_EMPTY", ex.getCode());
    }

    @Test
    void delete_when_empty_deletes() {
        UUID id = UUID.randomUUID();
        Mockito.when(equipeRepository.existsByPouleId(eq(id))).thenReturn(false);
        service.delete(id);
        Mockito.verify(pouleRepository).deleteById(eq(id));
    }

    @Test
    void get_returns_poule_with_team_count() {
        UUID id = UUID.randomUUID();
        Poule p = new Poule();
        p.setId(id);
        p.setNom("P");
        Mockito.when(pouleRepository.findById(eq(id))).thenReturn(Optional.of(p));
        Mockito.when(equipeRepository.countByPouleId(eq(id))).thenReturn(2);

        PouleResponse resp = service.get(id);
        assertEquals(2, resp.teamCount());
    }

    @Test
    void list_returns_page() {
        Poule p = new Poule();
        p.setId(UUID.randomUUID());
        p.setNom("P");
        Mockito.when(pouleRepository.findAll(eq(PageRequest.of(0, 10)))).thenReturn(new PageImpl<>(List.of(p)));
        Mockito.when(equipeRepository.countByPouleId(eq(p.getId()))).thenReturn(0);

        var page = service.list(PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void assignTeam_delegates_to_equipe_service() {
        UUID pouleId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        service.assignTeam(pouleId, teamId);
        Mockito.verify(equipeService).assignToPoule(eq(teamId), eq(pouleId));
    }

    @Test
    void removeTeam_when_not_in_poule_throws() {
        UUID pouleId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        TeamResponse resp = new TeamResponse(teamId, "Co", null, null, null);
        Mockito.when(equipeService.get(eq(teamId))).thenReturn(resp);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.removeTeam(pouleId, teamId));
        assertEquals("TEAM_NOT_IN_POULE", ex.getCode());
    }

    @Test
    void removeTeam_when_in_poule_calls_remove() {
        UUID pouleId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        TeamResponse resp = new TeamResponse(teamId, "Co", pouleId, null, null);
        Mockito.when(equipeService.get(eq(teamId))).thenReturn(resp);
        service.removeTeam(pouleId, teamId);
        Mockito.verify(equipeService).removeFromPoule(eq(teamId));
    }
}
