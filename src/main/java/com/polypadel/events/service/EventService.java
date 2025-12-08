package com.polypadel.events.service;

import com.polypadel.common.exception.BusinessException;
import com.polypadel.domain.entity.Equipe;
import com.polypadel.domain.entity.Evenement;
import com.polypadel.domain.entity.Match;
import com.polypadel.domain.enums.MatchStatus;
import com.polypadel.equipes.repository.EquipeRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final MatchRepository matchRepository;
    private final EquipeRepository equipeRepository; // Nécessaire pour lier les équipes
    private final EventMapper eventMapper;

    public EventService(EventRepository eventRepository, 
                        MatchRepository matchRepository, 
                        EquipeRepository equipeRepository,
                        EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.matchRepository = matchRepository;
        this.equipeRepository = equipeRepository;
        this.eventMapper = eventMapper;
    }

    // --- 1. CRÉATION (Événement + Matchs) ---
    @Transactional
    public EventResponse create(EventCreateRequest req) {
        // Validation Date >= Aujourd'hui
        if (req.eventDate().isBefore(LocalDate.now())) {
            throw new BusinessException("INVALID_DATE", "La date de l'événement ne peut pas être dans le passé");
        }

        // 1. Créer l'Événement
        Evenement e = new Evenement();
        e.setEventDate(req.eventDate());
        e.setEventTime(req.eventTime());
        e = eventRepository.save(e); // On sauvegarde pour avoir l'ID

        // 2. Créer les Matchs associés (boucle sur la liste fournie dans le JSON)
        List<Match> createdMatches = new ArrayList<>();
        
        for (EventCreateRequest.MatchSubRequest matchReq : req.matches()) {
            // Vérification simple : Equipes différentes
            if (matchReq.team1Id().equals(matchReq.team2Id())) {
                throw new BusinessException("MATCH_TEAMS_SAME", "Les équipes d'un match doivent être différentes");
            }

            Equipe t1 = equipeRepository.findById(matchReq.team1Id()).orElseThrow();
            Equipe t2 = equipeRepository.findById(matchReq.team2Id()).orElseThrow();

            // Vérification disponibilité Piste (pour ce créneau précis)
            if (matchRepository.existsByEvenementIdAndPisteAndStartTime(e.getId(), matchReq.courtNumber(), e.getEventTime())) {
                throw new BusinessException("MATCH_SLOT_TAKEN", "La piste " + matchReq.courtNumber() + " est déjà prise pour cet événement");
            }

            Match m = new Match();
            m.setEvenement(e);
            m.setEquipe1(t1);
            m.setEquipe2(t2);
            m.setPiste(matchReq.courtNumber());
            m.setStartTime(e.getEventTime()); // Le match prend l'heure de l'événement
            m.setStatut(MatchStatus.A_VENIR);
            
            createdMatches.add(matchRepository.save(m));
        }

        e.setMatches(createdMatches); // Mettre à jour la relation pour le retour
        return eventMapper.toResponse(e);
    }

    // --- 2. MISE À JOUR (Date/Heure uniquement) ---
    @Transactional
    public EventResponse update(UUID id, EventUpdateRequest req) {
        Evenement e = eventRepository.findById(id)
                .orElseThrow(() -> new BusinessException("EVENT_NOT_FOUND", "Événement introuvable"));

        // On ne permet la modif que si l'événement n'a pas commencé (règle simplifiée)
        // ou si on est admin. Ici on applique les changements simplement.
        
        if (req.eventDate() != null) {
            if (req.eventDate().isBefore(LocalDate.now())) {
                throw new BusinessException("INVALID_DATE", "La date ne peut pas être dans le passé");
            }
            e.setEventDate(req.eventDate());
        }
        
        if (req.eventTime() != null) {
            e.setEventTime(req.eventTime());
            // Attention : Idéalement il faudrait mettre à jour l'heure de tous les matchs associés ici
            // matchRepository.updateStartTimeForEvent(id, req.eventTime());
        }

        return eventMapper.toResponse(eventRepository.save(e));
    }

    // --- 3. SUPPRESSION ---
    @Transactional
    public void delete(UUID id) {
        // Spec: "Condition: Tous les matchs doivent avoir le statut A_VENIR"
        boolean hasStartedMatches = matchRepository.existsByEvenementIdAndStatut(id, MatchStatus.TERMINE) 
                                 || matchRepository.existsByEvenementIdAndStatut(id, MatchStatus.EN_COURS);
                                 
        if (hasStartedMatches) {
            throw new BusinessException("EVENT_DELETE_FORBIDDEN", "Impossible de supprimer un événement qui a des matchs terminés ou en cours");
        }

        // La cascade (si configurée dans l'entité) supprimera les matchs.
        // Sinon, on les supprime manuellement ici si besoin.
        eventRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public EventResponse get(UUID id) {
        return eventMapper.toResponse(eventRepository.findById(id).orElseThrow());
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> list(Pageable pageable) {
        return eventRepository.findAll(pageable).map(eventMapper::toResponse);
    }

    // --- 4. CALENDRIER ---
    @Transactional(readOnly = true)
    public List<EventResponse> calendar(LocalDate start, LocalDate end) {
        // Utilise la nouvelle méthode du Repository
        return eventMapper.toResponseList(
            eventRepository.findByEventDateBetweenOrderByEventDateAscEventTimeAsc(start, end)
        );
    }
}