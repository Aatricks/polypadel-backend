package com.polypadel.service;

import com.polypadel.dto.*;
import com.polypadel.model.*;
import com.polypadel.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;

@Service
public class MatchService {
    private final MatchRepository matchRepository;
    private final TeamService teamService;

    public MatchService(MatchRepository matchRepository, TeamService teamService) {
        this.matchRepository = matchRepository;
        this.teamService = teamService;
    }

    public List<MatchResponse> findUpcoming(Long teamId, Boolean myMatches, User currentUser) {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(30);
        List<Match> matches = matchRepository.findByDateRange(start, end);
        
        if (teamId != null) {
            matches = matches.stream().filter(m -> 
                m.getTeam1().getId().equals(teamId) || m.getTeam2().getId().equals(teamId)).toList();
        }
        return matches.stream().map(this::toResponse).toList();
    }

    public MatchResponse findById(Long id) {
        return toResponse(matchRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match non trouvé")));
    }

    public MatchResponse update(Long id, MatchUpdateRequest request) {
        Match match = matchRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match non trouvé"));
        
        if (request.status() != null) {
            match.setStatus(MatchStatus.valueOf(request.status()));
        }
        if (request.scoreTeam1() != null) {
            match.setScoreTeam1(request.scoreTeam1());
        }
        if (request.scoreTeam2() != null) {
            match.setScoreTeam2(request.scoreTeam2());
        }
        return toResponse(matchRepository.save(match));
    }

    public void delete(Long id) {
        Match match = matchRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match non trouvé"));
        if (match.getStatus() != MatchStatus.A_VENIR) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seuls les matchs à venir peuvent être supprimés");
        }
        matchRepository.delete(match);
    }

    private MatchResponse toResponse(Match m) {
        return new MatchResponse(m.getId(),
            new MatchResponse.EventInfo(m.getEvent().getEventDate(), m.getEvent().getEventTime()),
            m.getCourtNumber(),
            teamService.toResponse(m.getTeam1()),
            teamService.toResponse(m.getTeam2()),
            m.getStatus().name(),
            m.getScoreTeam1(),
            m.getScoreTeam2());
    }
}
