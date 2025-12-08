package com.polypadel.events;

import com.polypadel.common.exception.BusinessException;
import com.polypadel.domain.entity.Evenement;
import com.polypadel.domain.enums.MatchStatus;
import com.polypadel.equipes.repository.EquipeRepository;
import com.polypadel.events.dto.EventCreateRequest;
import com.polypadel.events.dto.EventResponse;
import com.polypadel.events.dto.EventUpdateRequest;
import com.polypadel.events.mapper.EventMapper;
import com.polypadel.events.repository.EventRepository;
import com.polypadel.events.service.EventService;
import com.polypadel.matches.repository.MatchRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class EventServiceTest {

    @Test
    void create_with_past_date_throws() {
        var eventRepo = Mockito.mock(EventRepository.class);
        var matchRepo = Mockito.mock(MatchRepository.class);
        var equipeRepo = Mockito.mock(EquipeRepository.class); // <--- Nouveau Mock
        var mapper = Mockito.mock(EventMapper.class);
        
        EventService svc = new EventService(eventRepo, matchRepo, equipeRepo, mapper);

        // Test : Date dans le passé
        EventCreateRequest req = new EventCreateRequest(LocalDate.now().minusDays(1), LocalTime.of(10, 0), new ArrayList<>());

        assertThrows(BusinessException.class, () -> svc.create(req));
    }

    @Test
    void create_ok() {
        var eventRepo = Mockito.mock(EventRepository.class);
        var matchRepo = Mockito.mock(MatchRepository.class);
        var equipeRepo = Mockito.mock(EquipeRepository.class);
        var mapper = Mockito.mock(EventMapper.class);
        
        EventService svc = new EventService(eventRepo, matchRepo, equipeRepo, mapper);

        // DTO avec Date future et Heure
        EventCreateRequest req = new EventCreateRequest(LocalDate.now().plusDays(1), LocalTime.of(10, 0), new ArrayList<>());
        
        Evenement e = new Evenement(); 
        e.setId(UUID.randomUUID()); 
        e.setEventDate(req.eventDate()); 
        e.setEventTime(req.eventTime());
        
        Mockito.when(eventRepo.save(Mockito.any())).thenReturn(e);
        // Simulation du Mapper
        Mockito.when(mapper.toResponse(e)).thenReturn(new EventResponse(e.getId(), e.getEventDate(), e.getEventTime(), new ArrayList<>()));

        EventResponse resp = svc.create(req);
        
        assertThat(resp).isNotNull();
        assertThat(resp.eventDate()).isEqualTo(req.eventDate());
    }

    @Test
    void update_ok() {
        var eventRepo = Mockito.mock(EventRepository.class);
        var matchRepo = Mockito.mock(MatchRepository.class);
        var equipeRepo = Mockito.mock(EquipeRepository.class);
        var mapper = Mockito.mock(EventMapper.class);
        
        EventService svc = new EventService(eventRepo, matchRepo, equipeRepo, mapper);

        // Update Request avec nouveaux champs
        EventUpdateRequest req = new EventUpdateRequest(LocalDate.now().plusDays(5), LocalTime.of(14, 0));
        UUID id = UUID.randomUUID();
        
        Evenement e = new Evenement(); 
        e.setId(id); 
        e.setEventDate(LocalDate.now()); 
        e.setEventTime(LocalTime.of(10, 0));
        
        Mockito.when(eventRepo.findById(id)).thenReturn(Optional.of(e));
        Mockito.when(eventRepo.save(Mockito.any())).thenAnswer(i -> i.getArgument(0));
        Mockito.when(mapper.toResponse(Mockito.any())).thenReturn(new EventResponse(id, req.eventDate(), req.eventTime(), new ArrayList<>()));

        EventResponse resp = svc.update(id, req);
        
        assertThat(resp.eventDate()).isEqualTo(req.eventDate());
        assertThat(resp.eventTime()).isEqualTo(req.eventTime());
    }

    @Test
    void update_not_found_throws() {
        var eventRepo = Mockito.mock(EventRepository.class);
        var matchRepo = Mockito.mock(MatchRepository.class);
        var equipeRepo = Mockito.mock(EquipeRepository.class);
        var mapper = Mockito.mock(EventMapper.class);
        EventService svc = new EventService(eventRepo, matchRepo, equipeRepo, mapper);

        EventUpdateRequest req = new EventUpdateRequest(LocalDate.now().plusDays(5), LocalTime.of(12, 0));
        UUID id = UUID.randomUUID();
        Mockito.when(eventRepo.findById(id)).thenReturn(Optional.empty());
        
        // On s'attend à une BusinessException "EVENT_NOT_FOUND" (selon votre nouveau code)
        assertThrows(BusinessException.class, () -> svc.update(id, req));
    }

    @Test
    void delete_ok_and_delete_fails_when_has_started_matches() {
        var eventRepo = Mockito.mock(EventRepository.class);
        var matchRepo = Mockito.mock(MatchRepository.class);
        var equipeRepo = Mockito.mock(EquipeRepository.class);
        var mapper = Mockito.mock(EventMapper.class);
        EventService svc = new EventService(eventRepo, matchRepo, equipeRepo, mapper);

        UUID id = UUID.randomUUID();

        // Cas 1 : Suppression OK (Pas de matchs terminés ou en cours)
        Mockito.when(matchRepo.existsByEvenementIdAndStatut(id, MatchStatus.TERMINE)).thenReturn(false);
        Mockito.when(matchRepo.existsByEvenementIdAndStatut(id, MatchStatus.EN_COURS)).thenReturn(false);
        
        svc.delete(id);
        Mockito.verify(eventRepo).deleteById(id);

        // Cas 2 : Suppression KO (Il y a des matchs terminés)
        Mockito.when(matchRepo.existsByEvenementIdAndStatut(id, MatchStatus.TERMINE)).thenReturn(true);
        
        assertThatThrownBy(() -> svc.delete(id))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo("EVENT_DELETE_FORBIDDEN"));
    }

    @Test
    void get_list_and_calendar_map_properly() {
        var eventRepo = Mockito.mock(EventRepository.class);
        var matchRepo = Mockito.mock(MatchRepository.class);
        var equipeRepo = Mockito.mock(EquipeRepository.class);
        var mapper = Mockito.mock(EventMapper.class);
        EventService svc = new EventService(eventRepo, matchRepo, equipeRepo, mapper);

        UUID id = UUID.randomUUID();
        Evenement e = new Evenement(); 
        e.setId(id); 
        e.setEventDate(LocalDate.now());
        e.setEventTime(LocalTime.of(10, 0));

        // Test GET
        Mockito.when(eventRepo.findById(id)).thenReturn(Optional.of(e));
        Mockito.when(mapper.toResponse(e)).thenReturn(new EventResponse(id, e.getEventDate(), e.getEventTime(), new ArrayList<>()));
        assertThat(svc.get(id)).isNotNull();

        // Test LIST
      //  Mockito.when(eventRepository(eventRepo.findAll(Mockito.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(e)));
        // Note: La méthode toResponse peut être appelée par map()
        assertThat(svc.list(Pageable.unpaged())).hasSize(1);

        // Test CALENDAR (Nouvelle méthode de repository)
        Mockito.when(eventRepo.findByEventDateBetweenOrderByEventDateAscEventTimeAsc(any(), any())).thenReturn(List.of(e));
       // Mockito.when(mapper.toResponseList(any())).thenReturn(List.of(new EventResponse(id, e.getEventDate(), e.getEventTime(), new ArrayList<>()));
        
        assertThat(svc.calendar(LocalDate.now(), LocalDate.now().plusDays(3))).hasSize(1);
    }
    
    // Helper pour cast le mock plus facilement dans la ligne complexe
    private org.springframework.data.domain.Page<Evenement> eventRepository(org.springframework.data.domain.Page<Evenement> page) {
        return page;
    }
}