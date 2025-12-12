package com.polypadel.service;

import com.polypadel.dto.*;
import com.polypadel.model.*;
import com.polypadel.repository.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class MatchService extends BaseService<Match, Long, MatchResponse> {
    private final MatchRepository matchRepository;
    private final TeamService teamService;
    private final TeamRepository teamRepository;
    private final EventRepository eventRepository;

    public MatchService(MatchRepository matchRepository, TeamService teamService,
                       TeamRepository teamRepository, EventRepository eventRepository) {
        this.matchRepository = matchRepository;
        this.teamService = teamService;
        this.teamRepository = teamRepository;
        this.eventRepository = eventRepository;
    }

    @Override protected JpaRepository<Match, Long> getRepository() { return matchRepository; }
    @Override protected String getEntityName() { return "Match"; }

    public List<MatchResponse> findUpcoming(Long teamId, Boolean myMatches, User currentUser) {
        List<Match> matches = matchRepository.findByDateRange(LocalDate.now(), LocalDate.now().plusDays(30));
        if (teamId != null) {
            matches = matches.stream().filter(m ->
                m.getTeam1().getId().equals(teamId) || m.getTeam2().getId().equals(teamId)).toList();
        }
        return matches.stream().map(this::toResponse).toList();
    }

    public MatchResponse create(MatchCreateRequest request) {
        Event event = eventRepository.findById(request.eventId())
            .orElseThrow(() -> badRequest("Événement non trouvé"));
        Team team1 = teamRepository.findById(request.team1Id())
            .orElseThrow(() -> badRequest("Équipe 1 non trouvée"));
        Team team2 = teamRepository.findById(request.team2Id())
            .orElseThrow(() -> badRequest("Équipe 2 non trouvée"));

        if (team1.getId().equals(team2.getId())) {
            throw badRequest("Les deux équipes doivent être différentes");
        }

        Match match = new Match();
        match.setEvent(event);
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setCourtNumber(request.courtNumber());
        match.setStatus(MatchStatus.A_VENIR);
        return toResponse(matchRepository.save(match));
    }

    public MatchResponse update(Long id, MatchUpdateRequest request) {
        Match match = getEntityById(id);
        if (request.status() != null) match.setStatus(MatchStatus.valueOf(request.status()));
        if (request.scoreTeam1() != null) {
            validateScore(request.scoreTeam1());
            match.setScoreTeam1(request.scoreTeam1());
        }
        if (request.scoreTeam2() != null) {
            validateScore(request.scoreTeam2());
            match.setScoreTeam2(request.scoreTeam2());
        }
        return toResponse(matchRepository.save(match));
    }

    @Override
    protected void validateDelete(Match match) {
        if (match.getStatus() != MatchStatus.A_VENIR) {
            throw conflict("Seuls les matchs à venir peuvent être supprimés");
        }
    }

    private void validateScore(String score) {
        if (!score.matches("^(\\d+-\\d+)(,\\s*\\d+-\\d+){1,2}$")) {
            throw badRequest("Format de score invalide (ex: 6-4, 6-3)");
        }
        String[] sets = score.split(",\\s*");
        for (String set : sets) {
            String[] points = set.split("-");
            int p1 = Integer.parseInt(points[0]);
            int p2 = Integer.parseInt(points[1]);
            if (Math.max(p1, p2) < 6) throw badRequest("Un set doit avoir au moins 6 jeux gagnés");
            if (Math.min(p1, p2) > 5 && Math.abs(p1 - p2) > 1) throw badRequest("Écart invalide dans le set");
        }
    }

    @Override
    public MatchResponse toResponse(Match m) {
        return new MatchResponse(m.getId(),
            new MatchResponse.EventInfo(m.getEvent().getEventDate(), m.getEvent().getEventTime()),
            m.getCourtNumber(), teamService.toResponse(m.getTeam1()), teamService.toResponse(m.getTeam2()),
            m.getStatus().name(), m.getScoreTeam1(), m.getScoreTeam2());
    }
}
