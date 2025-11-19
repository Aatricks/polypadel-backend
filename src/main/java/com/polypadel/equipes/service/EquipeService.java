package com.polypadel.equipes.service;

import com.polypadel.common.exception.BusinessException;
import com.polypadel.domain.entity.Equipe;
import com.polypadel.domain.entity.Joueur;
import com.polypadel.domain.entity.Poule;
import com.polypadel.domain.enums.MatchStatus;
import com.polypadel.equipes.dto.TeamCreateRequest;
import com.polypadel.equipes.dto.TeamResponse;
import com.polypadel.equipes.dto.TeamUpdateRequest;
import com.polypadel.equipes.mapper.EquipeMapper;
import com.polypadel.equipes.repository.EquipeRepository;
import com.polypadel.joueurs.repository.JoueurRepository;
import com.polypadel.matches.repository.MatchRepository;
import com.polypadel.poules.repository.PouleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EquipeService {

    private final EquipeRepository equipeRepository;
    private final JoueurRepository joueurRepository;
    private final PouleRepository pouleRepository;
    private final MatchRepository matchRepository;
    private final EquipeMapper equipeMapper;

    public EquipeService(EquipeRepository equipeRepository, JoueurRepository joueurRepository, PouleRepository pouleRepository, MatchRepository matchRepository, EquipeMapper equipeMapper) {
        this.equipeRepository = equipeRepository;
        this.joueurRepository = joueurRepository;
        this.pouleRepository = pouleRepository;
        this.matchRepository = matchRepository;
        this.equipeMapper = equipeMapper;
    }

    @Transactional
    public TeamResponse create(TeamCreateRequest req) {
        Joueur j1 = joueurRepository.findById(req.joueur1Id()).orElseThrow();
        Joueur j2 = joueurRepository.findById(req.joueur2Id()).orElseThrow();
        if (!j1.getEntreprise().equals(j2.getEntreprise()) || !j1.getEntreprise().equals(req.entreprise())) {
            throw new BusinessException("TEAM_DIFFERENT_ENTREPRISE", "Players must share enterprise");
        }
        if (equipeRepository.existsByJoueur1IdOrJoueur2Id(j1.getId(), j1.getId()) ||
            equipeRepository.existsByJoueur1IdOrJoueur2Id(j2.getId(), j2.getId())) {
            throw new BusinessException("PLAYER_ALREADY_IN_TEAM", "Player already in a team");
        }
        Equipe e = new Equipe();
        e.setEntreprise(req.entreprise());
        e.setJoueur1(j1);
        e.setJoueur2(j2);
        if (req.pouleId() != null) {
            Poule p = pouleRepository.findById(req.pouleId()).orElseThrow();
            if (equipeRepository.countByPouleId(p.getId()) >= 6) {
                throw new BusinessException("POULE_SIZE_VIOLATION", "Poule already has 6 teams");
            }
            e.setPoule(p);
        }
        return equipeMapper.toResponse(equipeRepository.save(e));
    }

    @Transactional
    public TeamResponse update(UUID id, TeamUpdateRequest req) {
        Equipe e = equipeRepository.findById(id).orElseThrow();
        ensureNotLocked(id);
        if (req.entreprise() != null) e.setEntreprise(req.entreprise());
        if (req.joueur1Id() != null) e.setJoueur1(joueurRepository.findById(req.joueur1Id()).orElseThrow());
        if (req.joueur2Id() != null) e.setJoueur2(joueurRepository.findById(req.joueur2Id()).orElseThrow());
        if (req.pouleId() != null) {
            Poule p = pouleRepository.findById(req.pouleId()).orElseThrow();
            if (!p.equals(e.getPoule()) && equipeRepository.countByPouleId(p.getId()) >= 6) {
                throw new BusinessException("POULE_SIZE_VIOLATION", "Poule already has 6 teams");
            }
            e.setPoule(p);
        }
        validateEnterpriseConsistency(e);
        return equipeMapper.toResponse(equipeRepository.save(e));
    }

    @Transactional
    public void delete(UUID id) {
        ensureNotLocked(id);
        equipeRepository.deleteById(id);
    }

    @Transactional
    public TeamResponse assignToPoule(UUID teamId, UUID pouleId) {
        Equipe e = equipeRepository.findById(teamId).orElseThrow();
        ensureNotLocked(teamId);
        Poule p = pouleRepository.findById(pouleId).orElseThrow();
        if (e.getPoule() == null || !p.getId().equals(e.getPoule().getId())) {
            if (equipeRepository.countByPouleId(p.getId()) >= 6) {
                throw new BusinessException("POULE_SIZE_VIOLATION", "Poule already has 6 teams");
            }
        }
        e.setPoule(p);
        return equipeMapper.toResponse(equipeRepository.save(e));
    }

    @Transactional
    public TeamResponse removeFromPoule(UUID teamId) {
        Equipe e = equipeRepository.findById(teamId).orElseThrow();
        ensureNotLocked(teamId);
        e.setPoule(null);
        return equipeMapper.toResponse(equipeRepository.save(e));
    }

    @Transactional(readOnly = true)
    public TeamResponse get(UUID id) {
        return equipeMapper.toResponse(equipeRepository.findById(id).orElseThrow());
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<TeamResponse> list(org.springframework.data.domain.Pageable pageable) {
        return equipeRepository.findAll(pageable).map(equipeMapper::toResponse);
    }

    private void ensureNotLocked(UUID teamId) {
        if (matchRepository.existsByStatutAndEquipe1IdOrStatutAndEquipe2Id(MatchStatus.TERMINE, teamId, MatchStatus.TERMINE, teamId)) {
            throw new BusinessException("TEAM_LOCKED", "Team has played matches");
        }
    }

    private void validateEnterpriseConsistency(Equipe e) {
        if (e.getJoueur1() != null && e.getJoueur2() != null) {
            var enterprise = e.getEntreprise();
            if (enterprise != null && (!enterprise.equals(e.getJoueur1().getEntreprise()) || !enterprise.equals(e.getJoueur2().getEntreprise()))) {
                throw new BusinessException("TEAM_DIFFERENT_ENTREPRISE", "Players must share enterprise");
            }
        }
    }
}
