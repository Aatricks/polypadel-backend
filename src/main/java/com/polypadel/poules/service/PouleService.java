package com.polypadel.poules.service;

import com.polypadel.common.exception.BusinessException;
import com.polypadel.domain.entity.Poule;
import com.polypadel.equipes.repository.EquipeRepository;
import com.polypadel.equipes.service.EquipeService;
import com.polypadel.poules.dto.PouleCreateRequest;
import com.polypadel.poules.dto.PouleResponse;
import com.polypadel.poules.dto.PouleUpdateRequest;
import com.polypadel.poules.repository.PouleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PouleService {

    private final PouleRepository pouleRepository;
    private final EquipeRepository equipeRepository;
    private final EquipeService equipeService;

    public PouleService(PouleRepository pouleRepository,
                        EquipeRepository equipeRepository,
                        EquipeService equipeService) {
        this.pouleRepository = pouleRepository;
        this.equipeRepository = equipeRepository;
        this.equipeService = equipeService;
    }

    @Transactional
    public PouleResponse create(PouleCreateRequest req) {
        Poule p = new Poule();
        p.setNom(req.nom.trim());
        return toResponse(pouleRepository.save(p));
    }

    @Transactional
    public PouleResponse update(UUID id, PouleUpdateRequest req) {
        Poule p = pouleRepository.findById(id).orElseThrow();
        p.setNom(req.nom.trim());
        return toResponse(pouleRepository.save(p));
    }

    @Transactional
    public void delete(UUID id) {
        if (equipeRepository.existsByPouleId(id)) {
            throw new BusinessException("POULE_NOT_EMPTY", "Cannot delete poule with teams assigned");
        }
        pouleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PouleResponse get(UUID id) {
        return toResponse(pouleRepository.findById(id).orElseThrow());
    }

    @Transactional(readOnly = true)
    public Page<PouleResponse> list(Pageable pageable) {
        return pouleRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public void assignTeam(UUID pouleId, UUID teamId) {
        equipeService.assignToPoule(teamId, pouleId);
    }

    @Transactional
    public void removeTeam(UUID pouleId, UUID teamId) {
        // ensure the team is currently in this poule
        var resp = equipeService.get(teamId);
        if (resp.pouleId == null || !resp.pouleId.equals(pouleId)) {
            throw new BusinessException("TEAM_NOT_IN_POULE", "Team is not in this poule");
        }
        equipeService.removeFromPoule(teamId);
    }

    private PouleResponse toResponse(Poule p) {
        PouleResponse r = new PouleResponse();
        r.id = p.getId();
        r.nom = p.getNom();
        r.teamCount = p.getId() == null ? 0 : equipeRepository.countByPouleId(p.getId());
        return r;
    }
}
