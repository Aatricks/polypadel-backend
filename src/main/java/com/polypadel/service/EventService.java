package com.polypadel.service;

import com.polypadel.dto.*;
import com.polypadel.model.*;
import com.polypadel.repository.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Service
public class EventService extends BaseService<Event, Long, EventResponse> {
    private final EventRepository eventRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final TeamService teamService;

    public EventService(EventRepository eventRepository, TeamRepository teamRepository,
                        MatchRepository matchRepository, TeamService teamService) {
        this.eventRepository = eventRepository;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.teamService = teamService;
    }

    @Override protected JpaRepository<Event, Long> getRepository() { return eventRepository; }
    @Override protected String getEntityName() { return "Événement"; }

    public List<EventResponse> findAll(LocalDate startDate, LocalDate endDate, String month) {
        List<Event> events;
        if (month != null) {
            LocalDate start = LocalDate.parse(month + "-01");
            events = eventRepository.findByEventDateBetween(start, start.plusMonths(1).minusDays(1));
        } else if (startDate != null && endDate != null) {
            events = eventRepository.findByEventDateBetween(startDate, endDate);
        } else {
            events = eventRepository.findAllWithMatches();
        }
        return events.stream().map(this::toResponse).toList();
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
            match.setTeam1(teamRepository.findById(mi.team1Id()).orElseThrow(() -> badRequest("Équipe non trouvée")));
            match.setTeam2(teamRepository.findById(mi.team2Id()).orElseThrow(() -> badRequest("Équipe non trouvée")));
            match.setStatus(MatchStatus.A_VENIR);
            matchRepository.save(match);
        }
        return findById(event.getId());
    }

    @Transactional
    public EventResponse update(Long id, EventRequest request) {
        Event event = getEntityById(id);
        event.setEventDate(request.eventDate());
        event.setEventTime(request.eventTime());
        return toResponse(eventRepository.save(event));
    }

    @Override
    protected void validateDelete(Event event) {
        if (event.getMatches().stream().anyMatch(m -> m.getStatus() != MatchStatus.A_VENIR)) {
            throw conflict("Suppression impossible: des matchs ont été joués");
        }
    }

    private void validateEventRequest(EventRequest request) {
        if (request.eventDate().isBefore(LocalDate.now())) {
            throw badRequest("La date de l'événement doit être aujourd'hui ou dans le futur");
        }
        Set<Integer> courts = new HashSet<>();
        Set<Long> teams = new HashSet<>();
        for (EventRequest.MatchInfo mi : request.matches()) {
            if (!courts.add(mi.courtNumber())) throw badRequest("Piste " + mi.courtNumber() + " utilisée plusieurs fois");
            if (mi.team1Id().equals(mi.team2Id())) throw badRequest("Une équipe ne peut pas jouer contre elle-même");
            if (!teams.add(mi.team1Id()) || !teams.add(mi.team2Id())) throw badRequest("Une équipe ne peut jouer qu'un match par événement");
        }
    }

    @Override
    public EventResponse toResponse(Event e) {
        List<MatchResponse> matches = e.getMatches().stream()
            .map(m -> new MatchResponse(m.getId(), new MatchResponse.EventInfo(e.getEventDate(), e.getEventTime()),
                m.getCourtNumber(), teamService.toResponse(m.getTeam1()), teamService.toResponse(m.getTeam2()),
                m.getStatus().name(), m.getScoreTeam1(), m.getScoreTeam2())).toList();
        return new EventResponse(e.getId(), e.getEventDate(), e.getEventTime(), matches);
    }
}
