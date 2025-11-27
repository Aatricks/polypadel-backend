package com.polypadel.joueurs.service;

import com.polypadel.common.exception.BusinessException;
import com.polypadel.domain.entity.Joueur;
import com.polypadel.equipes.repository.EquipeRepository;
import com.polypadel.joueurs.dto.PlayerCreateRequest;
import com.polypadel.joueurs.dto.PlayerResponse;
import com.polypadel.joueurs.dto.PlayerUpdateRequest;
import com.polypadel.joueurs.mapper.JoueurMapper;
import com.polypadel.joueurs.repository.JoueurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class JoueurServiceTest {

    private JoueurRepository joueurRepository;
    private EquipeRepository equipeRepository;
    private JoueurMapper joueurMapper;
    private JoueurService service;

    @BeforeEach
    void setUp() {
        joueurRepository = Mockito.mock(JoueurRepository.class);
        equipeRepository = Mockito.mock(EquipeRepository.class);
        joueurMapper = Mockito.mock(JoueurMapper.class);
        service = new JoueurService(joueurRepository, equipeRepository, joueurMapper);
    }

    @Test
    void create_successful() {
        PlayerCreateRequest req = new PlayerCreateRequest("ABC123", "Doe", "John", LocalDate.now().minusYears(20), null, "Entreprise");
        Mockito.when(joueurRepository.findByNumLicence(eq("ABC123"))).thenReturn(Optional.empty());
        Joueur jSaved = new Joueur();
        jSaved.setId(UUID.randomUUID());
        jSaved.setNumLicence(req.numLicence());
        jSaved.setNom(req.nom());
        jSaved.setPrenom(req.prenom());
        jSaved.setDateNaissance(req.dateNaissance());
        jSaved.setEntreprise(req.entreprise());
        Mockito.when(joueurRepository.save(any(Joueur.class))).thenReturn(jSaved);
        PlayerResponse resp = new PlayerResponse(jSaved.getId(), jSaved.getNumLicence(), jSaved.getNom(), jSaved.getPrenom(), jSaved.getDateNaissance(), null, jSaved.getEntreprise());
        Mockito.when(joueurMapper.toResponse(jSaved)).thenReturn(resp);

        PlayerResponse out = service.create(req);
        assertEquals(resp, out);
        Mockito.verify(joueurRepository).save(any(Joueur.class));
        Mockito.verify(joueurMapper).toResponse(jSaved);
    }

    @Test
    void create_duplicate_numLicence_throws() {
        PlayerCreateRequest req = new PlayerCreateRequest("ABC123", "Doe", "John", LocalDate.now().minusYears(20), null, "Entreprise");
        Mockito.when(joueurRepository.findByNumLicence(eq("ABC123"))).thenReturn(Optional.of(new Joueur()));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req));
        assertEquals("PLAYER_NUM_LICENSE_EXISTS", ex.getCode());
    }

    @Test
    void create_invalid_age_future_throws() {
        PlayerCreateRequest req = new PlayerCreateRequest("ABC123", "Doe", "John", LocalDate.now().plusDays(1), null, "Entreprise");
        Mockito.when(joueurRepository.findByNumLicence(eq("ABC123"))).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req));
        assertEquals("PROFILE_DOB_INVALID", ex.getCode());
    }

    @Test
    void create_invalid_age_under16_throws() {
        PlayerCreateRequest req = new PlayerCreateRequest("ABC123", "Doe", "John", LocalDate.now().minusYears(10), null, "Entreprise");
        Mockito.when(joueurRepository.findByNumLicence(eq("ABC123"))).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req));
        assertEquals("PROFILE_DOB_AGE", ex.getCode());
    }

    @Test
    void update_successful_and_trims_date() {
        UUID id = UUID.randomUUID();
        Joueur existing = new Joueur();
        existing.setId(id);
        existing.setNom("Old");
        existing.setPrenom("Old");
        existing.setEntreprise("OldCo");
        Mockito.when(joueurRepository.findById(eq(id))).thenReturn(Optional.of(existing));

        PlayerUpdateRequest req = new PlayerUpdateRequest("NewName", null, LocalDate.now().minusYears(25), null, "NewCo");
        Mockito.when(joueurRepository.save(existing)).thenReturn(existing);
        PlayerResponse resp = new PlayerResponse(id, "", "NewName", "Old", req.dateNaissance(), null, req.entreprise());
        Mockito.when(joueurMapper.toResponse(existing)).thenReturn(resp);

        PlayerResponse out = service.update(id, req);
        assertEquals(resp, out);
        assertEquals("NewName", existing.getNom());
        assertEquals("NewCo", existing.getEntreprise());
    }

    @Test
    void update_not_found_throws() {
        UUID id = UUID.randomUUID();
        Mockito.when(joueurRepository.findById(eq(id))).thenReturn(Optional.empty());
        PlayerUpdateRequest req = new PlayerUpdateRequest(null, null, null, null, null);
        assertThrows(java.util.NoSuchElementException.class, () -> service.update(id, req));
    }

    @Test
    void delete_when_assigned_throws() {
        UUID id = UUID.randomUUID();
        Mockito.when(equipeRepository.playerInAnyTeam(eq(id))).thenReturn(true);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(id));
        assertEquals("PLAYER_IN_TEAM", ex.getCode());
    }

    @Test
    void delete_when_not_assigned_deletes() {
        UUID id = UUID.randomUUID();
        Mockito.when(equipeRepository.playerInAnyTeam(eq(id))).thenReturn(false);
        service.delete(id);
        Mockito.verify(joueurRepository).deleteById(eq(id));
    }

    @Test
    void get_returns_player() {
        UUID id = UUID.randomUUID();
        Joueur j = new Joueur();
        j.setId(id);
        Mockito.when(joueurRepository.findById(eq(id))).thenReturn(Optional.of(j));
        PlayerResponse resp = new PlayerResponse(id, "NUM", "N", "P", LocalDate.now().minusYears(20), null, "Co");
        Mockito.when(joueurMapper.toResponse(j)).thenReturn(resp);

        PlayerResponse out = service.get(id);
        assertEquals(resp, out);
    }

    @Test
    void search_with_null_query_uses_empty_string() {
        Pageable pg = PageRequest.of(0, 10);
        Joueur j = new Joueur();
        j.setId(UUID.randomUUID());
        Mockito.when(joueurRepository.search(eq(""), eq(pg))).thenReturn(new PageImpl<>(List.of(j)));
        PlayerResponse resp = new PlayerResponse(j.getId(), "NUM", "N", "P", LocalDate.now().minusYears(20), null, "Co");
        Mockito.when(joueurMapper.toResponse(any(Joueur.class))).thenReturn(resp);

        var page = service.search(null, pg);
        assertEquals(1, page.getTotalElements());
        assertEquals(resp, page.getContent().get(0));
    }
}
