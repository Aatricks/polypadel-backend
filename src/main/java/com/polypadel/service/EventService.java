package com.polypadel.service;

import com.polypadel.dto.*;
import com.polypadel.model.*;
import com.polypadel.repository.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final TeamService teamService;

    public EventService(
        EventRepository eventRepository,
        TeamRepository teamRepository,
        MatchRepository matchRepository,
        TeamService teamService
    ) {
        this.eventRepository = eventRepository;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.teamService = teamService;
    }

    public List<EventResponse> findAll(
        LocalDate startDate,
        LocalDate endDate,
        String month
    ) {
        List<Event> events;
        if (month != null) {
            LocalDate start = LocalDate.parse(month + "-01");
            LocalDate end = start.plusMonths(1).minusDays(1);
            events = eventRepository.findByEventDateBetween(start, end);
        } else if (startDate != null && endDate != null) {
            events = eventRepository.findByEventDateBetween(startDate, endDate);
        } else {
            events = eventRepository.findAllWithMatches();
        }
        return events.stream().map(this::toResponse).toList();
    }

    public EventResponse findById(Long id) {
        return toResponse(
            eventRepository
                .findById(id)
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Événement non trouvé"
                    )
                )
        );
    }

    @Transactional
    public EventResponse create(EventRequest request) {
        validateEventRequest(request);

        Event event = new Event();
        event.setEventDate(request.eventDate());
        event.setEventTime(request.eventTime());
        event = eventRepository.save(event);

        for (EventRequest.MatchInfo mi : request.matches()) {
            Match match = new Match();
            match.setEvent(event);
            match.setCourtNumber(mi.courtNumber());
            match.setTeam1(
                teamRepository
                    .findById(mi.team1Id())
                    .orElseThrow(() ->
                        new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Équipe non trouvée"
                        )
                    )
            );
            match.setTeam2(
                teamRepository
                    .findById(mi.team2Id())
                    .orElseThrow(() ->
                        new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Équipe non trouvée"
                        )
                    )
            );
            match.setStatus(MatchStatus.A_VENIR);
            matchRepository.save(match);
        }
        return findById(event.getId());
    }

    @Transactional
    public EventResponse update(Long id, EventRequest request) {
        Event event = eventRepository
            .findById(id)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Événement non trouvé"
                )
            );
        event.setEventDate(request.eventDate());
        event.setEventTime(request.eventTime());
        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public void delete(Long id) {
        Event event = eventRepository
            .findById(id)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Événement non trouvé"
                )
            );
        boolean hasPlayedMatches = event
            .getMatches()
            .stream()
            .anyMatch(m -> m.getStatus() != MatchStatus.A_VENIR);
        if (hasPlayedMatches) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Suppression impossible: des matchs ont été joués"
            );
        }
        eventRepository.delete(event);
    }

    private void validateEventRequest(EventRequest request) {
        Set<Integer> courts = new HashSet<>();
        Set<Long> teams = new HashSet<>();
        for (EventRequest.MatchInfo mi : request.matches()) {
            if (!courts.add(mi.courtNumber())) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Piste " + mi.courtNumber() + " utilisée plusieurs fois"
                );
            }
            if (mi.team1Id().equals(mi.team2Id())) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Une équipe ne peut pas jouer contre elle-même"
                );
            }
            if (!teams.add(mi.team1Id()) || !teams.add(mi.team2Id())) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Une équipe ne peut jouer qu'un match par événement"
                );
            }
        }
    }

    private EventResponse toResponse(Event e) {
        List<MatchResponse> matches = e
            .getMatches()
            .stream()
            .map(m ->
                new MatchResponse(
                    m.getId(),
                    new MatchResponse.EventInfo(
                        e.getEventDate(),
                        e.getEventTime()
                    ),
                    m.getCourtNumber(),
                    teamService.toResponse(m.getTeam1()),
                    teamService.toResponse(m.getTeam2()),
                    m.getStatus().name(),
                    m.getScoreTeam1(),
                    m.getScoreTeam2()
                )
            )
            .toList();
        return new EventResponse(
            e.getId(),
            e.getEventDate(),
            e.getEventTime(),
            matches
        );
    }
}
