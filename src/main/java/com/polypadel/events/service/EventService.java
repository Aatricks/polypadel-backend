package com.polypadel.events.service;

import com.polypadel.common.exception.BusinessException;
import com.polypadel.common.exception.ErrorCodes;
import com.polypadel.common.exception.NotFoundException;
import com.polypadel.domain.entity.Evenement;
import com.polypadel.events.dto.EventCreateRequest;
import com.polypadel.events.dto.EventResponse;
import com.polypadel.events.dto.EventUpdateRequest;
import com.polypadel.events.mapper.EventMapper;
import com.polypadel.events.repository.EventRepository;
import com.polypadel.matches.repository.MatchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final MatchRepository matchRepository;
    private final EventMapper eventMapper;

    public EventService(EventRepository eventRepository, MatchRepository matchRepository, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.matchRepository = matchRepository;
        this.eventMapper = eventMapper;
    }

    @Transactional
    public EventResponse create(EventCreateRequest req) {
        validateDates(req.dateDebut(), req.dateFin());
        Evenement e = new Evenement();
        e.setDateDebut(req.dateDebut());
        e.setDateFin(req.dateFin());
        return eventMapper.toResponse(eventRepository.save(e));
    }

    @Transactional
    public EventResponse update(UUID id, EventUpdateRequest req) {
        validateDates(req.dateDebut(), req.dateFin());
        Evenement e = eventRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.EVENT_NOT_FOUND, "Event not found: " + id));
        e.setDateDebut(req.dateDebut());
        e.setDateFin(req.dateFin());
        return eventMapper.toResponse(eventRepository.save(e));
    }

    @Transactional
    public void delete(UUID id) {
        if (matchRepository.countByEvenementId(id) > 0) {
            throw new BusinessException("EVENT_HAS_MATCHES", "Cannot delete event with matches");
        }
        eventRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public EventResponse get(UUID id) {
        return eventMapper.toResponse(eventRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.EVENT_NOT_FOUND, "Event not found: " + id)));
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> list(Pageable pageable) {
        return eventRepository.findAll(pageable).map(eventMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> calendar(LocalDate start, LocalDate end) {
        return eventMapper.toResponseList(eventRepository.findInRange(start, end));
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            throw new BusinessException("INVALID_DATES", "dateFin must be on or after dateDebut");
        }
    }
}
