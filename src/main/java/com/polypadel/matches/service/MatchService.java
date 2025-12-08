package com.polypadel.matches.service;

import com.polypadel.common.exception.BusinessException;
import com.polypadel.common.exception.ErrorCodes;
import com.polypadel.common.exception.NotFoundException;
import com.polypadel.domain.entity.Equipe;
import com.polypadel.domain.entity.Evenement;
import com.polypadel.domain.entity.Match;
import com.polypadel.domain.enums.MatchStatus;
import com.polypadel.equipes.repository.EquipeRepository;
import com.polypadel.events.repository.EventRepository;
import com.polypadel.joueurs.repository.JoueurRepository;
import com.polypadel.matches.dto.MatchCreateRequest;
import com.polypadel.matches.dto.MatchResponse;
import com.polypadel.matches.dto.MatchUpdateScoreRequest;
import com.polypadel.matches.mapper.MatchMapper;
import com.polypadel.matches.repository.MatchRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final EventRepository eventRepository;
    private final EquipeRepository equipeRepository;
    private final JoueurRepository joueurRepository;
    private final MatchMapper matchMapper;

    public MatchService(MatchRepository matchRepository,
                        EventRepository eventRepository,
                        EquipeRepository equipeRepository,
                        JoueurRepository joueurRepository,
                        MatchMapper matchMapper) {
        this.matchRepository = matchRepository;
        this.eventRepository = eventRepository;
        this.equipeRepository = equipeRepository;
        this.joueurRepository = joueurRepository;
        this.matchMapper = matchMapper;
    }

    @Transactional
    public MatchResponse create(MatchCreateRequest req) {
        if (req.equipe1Id().equals(req.equipe2Id())) {
            throw new BusinessException("MATCH_TEAMS_SAME", "Teams must be different");
        }
        Evenement event = eventRepository.findById(req.evenementId()).orElseThrow(() -> new NotFoundException(ErrorCodes.EVENT_NOT_FOUND, "Event not found: " + req.evenementId()));
        Equipe t1 = equipeRepository.findById(req.equipe1Id()).orElseThrow(() -> new NotFoundException(ErrorCodes.TEAM_NOT_FOUND, "Team not found: " + req.equipe1Id()));
        Equipe t2 = equipeRepository.findById(req.equipe2Id()).orElseThrow(() -> new NotFoundException(ErrorCodes.TEAM_NOT_FOUND, "Team not found: " + req.equipe2Id()));

        if (matchRepository.existsByEvenementIdAndPisteAndStartTime(event.getId(), req.piste(), req.startTime())) {
            throw new BusinessException("MATCH_SLOT_TAKEN", "This time slot and piste are already used");
        }

        if (matchRepository.existsByEvenementIdAndEquipe1IdOrEvenementIdAndEquipe2Id(event.getId(), t1.getId(), event.getId(), t1.getId()) ||
            matchRepository.existsByEvenementIdAndEquipe1IdOrEvenementIdAndEquipe2Id(event.getId(), t2.getId(), event.getId(), t2.getId())) {
            throw new BusinessException("TEAM_ALREADY_IN_EVENT", "A team can only play once in an event");
        }

        Match m = new Match();
        m.setEvenement(event);
        m.setEquipe1(t1);
        m.setEquipe2(t2);
        m.setPiste(req.piste());
        m.setStartTime(req.startTime());
        m.setStatut(MatchStatus.A_VENIR);
        return matchMapper.toResponse(matchRepository.save(m));
    }

    @Transactional
    public MatchResponse updateScore(UUID id, MatchUpdateScoreRequest req) {
        Match m = matchRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.MATCH_NOT_FOUND, "Match not found: " + id));
        if (req.statut() != null) {
            m.setStatut(req.statut());
        }
        if (m.getStatut() == MatchStatus.TERMINE) {
            ScoreValidator.validate(req.score1(), req.score2());
        }
        if (req.score1() != null) m.setScore1(req.score1());
        if (req.score2() != null) m.setScore2(req.score2());
        return matchMapper.toResponse(matchRepository.save(m));
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> upcomingForCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(auth.getName());
        var joueur = joueurRepository.findByUtilisateurId(userId).orElseThrow(() -> new NotFoundException(com.polypadel.common.exception.ErrorCodes.PLAYER_NOT_FOUND, "Player not found for user: " + userId));
        var teamIds = equipeRepository.findIdsByPlayer(joueur.getId());
        if (teamIds.isEmpty()) return List.of();
        var statuses = List.of(MatchStatus.A_VENIR, MatchStatus.EN_COURS);
        return matchRepository.findUpcomingForTeams(statuses, teamIds).stream().map(matchMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> listByEvent(UUID eventId) {
        return matchRepository.findByEvenementIdOrderByStartTimeAsc(eventId).stream().map(matchMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> finishedForCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(auth.getName());
        var joueur = joueurRepository.findByUtilisateurId(userId).orElseThrow(() -> new NotFoundException(com.polypadel.common.exception.ErrorCodes.PLAYER_NOT_FOUND, "Player not found for user: " + userId));
        var teamIds = equipeRepository.findIdsByPlayer(joueur.getId());
        if (teamIds.isEmpty()) return List.of();
        var statuses = List.of(MatchStatus.TERMINE);
        return matchRepository.findUpcomingForTeams(statuses, teamIds).stream().map(matchMapper::toResponse).toList();
    }
}
