package com.polypadel.events;

import com.polypadel.common.exception.BusinessException;
import com.polypadel.events.dto.EventCreateRequest;
import com.polypadel.events.dto.EventResponse;
import com.polypadel.events.dto.EventUpdateRequest;
import com.polypadel.events.repository.EventRepository;
import com.polypadel.events.service.EventService;
import com.polypadel.events.mapper.EventMapper;
import com.polypadel.matches.repository.MatchRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EventServiceTest {

    @Test
    void create_with_invalid_dates_throws() {
        var eventRepo = Mockito.mock(EventRepository.class);
        var matchRepo = Mockito.mock(MatchRepository.class);
        EventService svc = new EventService(eventRepo, matchRepo, Mockito.mock(EventMapper.class));

        EventCreateRequest req = new EventCreateRequest(LocalDate.now(), LocalDate.now().minusDays(1));

        assertThrows(BusinessException.class, () -> svc.create(req));
    }

    @Test
    void create_ok() {
        var eventRepo = Mockito.mock(EventRepository.class);
        var matchRepo = Mockito.mock(MatchRepository.class);
        var mapper = Mockito.mock(EventMapper.class);
        EventService svc = new EventService(eventRepo, matchRepo, mapper);

        EventCreateRequest req = new EventCreateRequest(LocalDate.now(), LocalDate.now().plusDays(1));
        var e = new com.polypadel.domain.entity.Evenement(); e.setId(UUID.randomUUID()); e.setDateDebut(req.dateDebut()); e.setDateFin(req.dateFin());
        Mockito.when(eventRepo.save(Mockito.any())).thenReturn(e);
        Mockito.when(mapper.toResponse(e)).thenReturn(new EventResponse(e.getId(), e.getDateDebut(), e.getDateFin()));

        EventResponse resp = svc.create(req);
        assertThat(resp).isNotNull();
        assertThat(resp.dateDebut()).isEqualTo(req.dateDebut());
    }

    @Test
    void update_ok() {
        var eventRepo = Mockito.mock(EventRepository.class);
        var matchRepo = Mockito.mock(MatchRepository.class);
        var mapper = Mockito.mock(EventMapper.class);
        EventService svc = new EventService(eventRepo, matchRepo, mapper);

        EventUpdateRequest req = new EventUpdateRequest(LocalDate.now().plusDays(5), LocalDate.now().plusDays(6));
        var id = UUID.randomUUID();
        var e = new com.polypadel.domain.entity.Evenement(); e.setId(id); e.setDateDebut(LocalDate.now()); e.setDateFin(LocalDate.now().plusDays(1));
        Mockito.when(eventRepo.findById(id)).thenReturn(Optional.of(e));
        Mockito.when(eventRepo.save(Mockito.any())).thenAnswer(i -> i.getArgument(0));
        Mockito.when(mapper.toResponse(Mockito.any())).thenReturn(new EventResponse(id, req.dateDebut(), req.dateFin()));

        EventResponse resp = svc.update(id, req);
        assertThat(resp.dateDebut()).isEqualTo(req.dateDebut());
    }

    @Test
    void update_not_found_throws() {
        var eventRepo = Mockito.mock(EventRepository.class);
        var matchRepo = Mockito.mock(MatchRepository.class);
        var mapper = Mockito.mock(EventMapper.class);
        EventService svc = new EventService(eventRepo, matchRepo, mapper);

        EventUpdateRequest req = new EventUpdateRequest(LocalDate.now().plusDays(5), LocalDate.now().plusDays(6));
        var id = UUID.randomUUID();
        Mockito.when(eventRepo.findById(id)).thenReturn(Optional.empty());
        org.junit.jupiter.api.Assertions.assertThrows(com.polypadel.common.exception.NotFoundException.class, () -> svc.update(id, req));
    }

    @Test
    void create_with_null_dates_throws() {
        var eventRepo = Mockito.mock(EventRepository.class);
        var matchRepo = Mockito.mock(MatchRepository.class);
        EventService svc = new EventService(eventRepo, matchRepo, Mockito.mock(EventMapper.class));

        EventCreateRequest reqNullStart = new EventCreateRequest(null, LocalDate.now());
        assertThrows(BusinessException.class, () -> svc.create(reqNullStart));

        EventCreateRequest reqNullEnd = new EventCreateRequest(LocalDate.now(), null);
        assertThrows(BusinessException.class, () -> svc.create(reqNullEnd));
    }

    @Test
    void delete_ok_and_delete_fails_when_has_matches() {
        var eventRepo = Mockito.mock(EventRepository.class);
        var matchRepo = Mockito.mock(MatchRepository.class);
        var mapper = Mockito.mock(EventMapper.class);
        EventService svc = new EventService(eventRepo, matchRepo, mapper);

        UUID id = UUID.randomUUID();
        Mockito.when(matchRepo.countByEvenementId(id)).thenReturn(0L);
        svc.delete(id);
        Mockito.verify(eventRepo).deleteById(id);

        Mockito.when(matchRepo.countByEvenementId(id)).thenReturn(2L);
        assertThatThrownBy(() -> svc.delete(id)).isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo("EVENT_HAS_MATCHES"));
    }

    @Test
    void get_list_and_calendar_map_properly() {
        var eventRepo = Mockito.mock(EventRepository.class);
        var matchRepo = Mockito.mock(MatchRepository.class);
        var mapper = Mockito.mock(EventMapper.class);
        EventService svc = new EventService(eventRepo, matchRepo, mapper);

        UUID id = UUID.randomUUID();
        var e = new com.polypadel.domain.entity.Evenement(); e.setId(id); e.setDateDebut(LocalDate.now()); e.setDateFin(LocalDate.now().plusDays(1));
        Mockito.when(eventRepo.findById(id)).thenReturn(Optional.of(e));
        Mockito.when(mapper.toResponse(e)).thenReturn(new EventResponse(id, e.getDateDebut(), e.getDateFin()));
        assertThat(svc.get(id)).isNotNull();

        Mockito.when(eventRepo.findAll(Mockito.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(e)));
        Mockito.when(mapper.toResponseList(Mockito.any())).thenReturn(List.of(new EventResponse(id, e.getDateDebut(), e.getDateFin())));
        assertThat(svc.list(Pageable.unpaged())).hasSize(1);

        Mockito.when(eventRepo.findInRange(Mockito.any(), Mockito.any())).thenReturn(List.of(e));
        Mockito.when(mapper.toResponseList(Mockito.any())).thenReturn(List.of(new EventResponse(id, e.getDateDebut(), e.getDateFin())));
        assertThat(svc.calendar(LocalDate.now(), LocalDate.now().plusDays(3))).hasSize(1);
    }
}
