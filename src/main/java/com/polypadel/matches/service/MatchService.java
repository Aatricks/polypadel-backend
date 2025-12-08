package com.polypadel.matches.service;

import com.polypadel.common.exception.BusinessException;
import com.polypadel.domain.entity.Equipe;
import com.polypadel.domain.entity.Evenement;
import com.polypadel.domain.entity.Match;
import com.polypadel.domain.enums.MatchStatus;
import com.polypadel.equipes.repository.EquipeRepository;
import com.polypadel.events.repository.EventRepository;
import com.polypadel.joueurs.repository.JoueurRepository;
import com.polypadel.matches.dto.MatchCreateRequest;
import com.polypadel.matches.dto.MatchResponse;
import com.polypadel.matches.dto.MatchUpdateRequest;
import com.polypadel.matches.repository.MatchRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final EventRepository eventRepository;
    private final EquipeRepository equipeRepository;
    private final JoueurRepository joueurRepository;

    public MatchService(MatchRepository matchRepository,
                        EventRepository eventRepository,
                        EquipeRepository equipeRepository,
                        JoueurRepository joueurRepository) {
        this.matchRepository = matchRepository;
        this.eventRepository = eventRepository;
        this.equipeRepository = equipeRepository;
        this.joueurRepository = joueurRepository;
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> searchMatches(boolean upcoming, boolean myMatches, UUID teamId, String statusStr) {
        List<Match> matches;

        if (myMatches) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UUID userId = UUID.fromString(auth.getName());
            var joueur = joueurRepository.findByUtilisateurId(userId)
                    .orElseThrow(() -> new BusinessException("USER_NOT_PLAYER", "L'utilisateur n'est pas un joueur"));
            List<UUID> teamIds = equipeRepository.findIdsByPlayer(joueur.getId());

            if (teamIds.isEmpty()) return List.of();
            matches = matchRepository.findByEquipe1IdInOrEquipe2IdIn(teamIds, teamIds);
        } else if (teamId != null) {
            matches = matchRepository.findByEquipe1IdOrEquipe2Id(teamId, teamId);
        } else {
            matches = matchRepository.findAll();
        }

        return matches.stream()
                .filter(m -> {
                    if (statusStr != null && !statusStr.isBlank()) {
                        return m.getStatut().name().equals(statusStr);
                    }
                    return true;
                })
                .filter(m -> {
                    if (upcoming) {
                        LocalDateTime now = LocalDateTime.now();
                        LocalDateTime limit = now.plusDays(30);
                        
                        LocalDate date = m.getEvenement().getEventDate();
                        LocalTime time = m.getStartTime();
                        LocalDateTime matchDateTime = date.atTime(time);

                        return matchDateTime.isAfter(now) && matchDateTime.isBefore(limit);
                    }
                    return true;
                })
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MatchResponse create(MatchCreateRequest req) {
        if (req.equipe1Id().equals(req.equipe2Id())) {
            throw new BusinessException("MATCH_TEAMS_SAME", "Les équipes doivent être différentes");
        }
        Evenement event = eventRepository.findById(req.evenementId()).orElseThrow();
        Equipe t1 = equipeRepository.findById(req.equipe1Id()).orElseThrow();
        Equipe t2 = equipeRepository.findById(req.equipe2Id()).orElseThrow();

        LocalTime time = req.startTime().toLocalTime();

        if (matchRepository.existsByEvenementIdAndPisteAndStartTime(event.getId(), req.piste(), time)) {
            throw new BusinessException("MATCH_SLOT_TAKEN", "Ce créneau et cette piste sont déjà pris");
        }

        if (isTeamBusy(event.getId(), t1.getId()) || isTeamBusy(event.getId(), t2.getId())) {
            throw new BusinessException("TEAM_ALREADY_IN_EVENT", "Une équipe ne peut jouer qu'une fois par événement");
        }

        Match m = new Match();
        m.setEvenement(event);
        m.setEquipe1(t1);
        m.setEquipe2(t2);
        m.setPiste(req.piste());
        m.setStartTime(time);
        m.setStatut(MatchStatus.A_VENIR);
        
        return toResponse(matchRepository.save(m));
    }

    @Transactional
    public MatchResponse update(UUID id, MatchUpdateRequest req) {
        Match m = matchRepository.findById(id)
                .orElseThrow(() -> new BusinessException("MATCH_NOT_FOUND", "Match introuvable"));

        if (req.courtNumber() != null && !req.courtNumber().equals(m.getPiste())) {
            boolean busy = matchRepository.existsByEvenementIdAndPisteAndStartTime(
                    m.getEvenement().getId(), req.courtNumber(), m.getStartTime());
            if (busy) {
                throw new BusinessException("MATCH_SLOT_TAKEN", "La piste " + req.courtNumber() + " est déjà occupée");
            }
            m.setPiste(req.courtNumber());
        }

        if (req.status() != null) {
            m.setStatut(req.status());
        }

        if (req.scoreTeam1() != null) m.setScore1(req.scoreTeam1());
        if (req.scoreTeam2() != null) m.setScore2(req.scoreTeam2());

        if (m.getStatut() == MatchStatus.TERMINE) {
            if (m.getScore1() == null || m.getScore2() == null) {
                throw new BusinessException("SCORE_MISSING", "Les scores sont obligatoires pour un match terminé");
            }
            ScoreValidator.validate(m.getScore1(), m.getScore2());
        }

        return toResponse(matchRepository.save(m));
    }

    @Transactional
    public void delete(UUID id) {
        Match m = matchRepository.findById(id)
                .orElseThrow(() -> new BusinessException("MATCH_NOT_FOUND", "Match introuvable"));

        if (m.getStatut() != MatchStatus.A_VENIR) {
            throw new BusinessException("DELETE_FORBIDDEN", "Impossible de supprimer un match terminé ou annulé");
        }

        matchRepository.delete(m);
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> listByEvent(UUID eventId) {
        return matchRepository.findByEvenementIdOrderByStartTimeAsc(eventId)
                .stream().map(this::toResponse).toList();
    }
    
    @Transactional(readOnly = true)
    public List<MatchResponse> finishedForCurrentUser() {
        return searchMatches(false, true, null, "TERMINE");
    }

    private boolean isTeamBusy(UUID eventId, UUID teamId) {
        return matchRepository.existsByEvenementIdAndEquipe1IdOrEvenementIdAndEquipe2Id(eventId, teamId, eventId, teamId);
    }

    private MatchResponse toResponse(Match m) {
        MatchResponse r = new MatchResponse();
        r.id = m.getId();
        r.evenementId = m.getEvenement().getId();
        r.equipe1Id = m.getEquipe1().getId();
        r.equipe2Id = m.getEquipe2().getId();
        r.piste = m.getPiste();
        
        if (m.getEvenement() != null) {
             r.startTime = m.getEvenement().getEventDate().atTime(m.getStartTime());
        }
        
        r.statut = m.getStatut();
        r.score1 = m.getScore1();
        r.score2 = m.getScore2();
        return r;
    }
}