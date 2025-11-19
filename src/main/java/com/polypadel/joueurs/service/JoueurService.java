package com.polypadel.joueurs.service;

import com.polypadel.common.exception.BusinessException;
import com.polypadel.domain.entity.Joueur;
import com.polypadel.equipes.repository.EquipeRepository;
import com.polypadel.joueurs.dto.PlayerCreateRequest;
import com.polypadel.joueurs.dto.PlayerResponse;
import com.polypadel.joueurs.dto.PlayerUpdateRequest;
import com.polypadel.joueurs.mapper.JoueurMapper;
import com.polypadel.joueurs.repository.JoueurRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

@Service
public class JoueurService {

    private final JoueurRepository joueurRepository;
    private final EquipeRepository equipeRepository;
    private final JoueurMapper joueurMapper;

    public JoueurService(JoueurRepository joueurRepository, EquipeRepository equipeRepository, JoueurMapper joueurMapper) {
        this.joueurRepository = joueurRepository;
        this.equipeRepository = equipeRepository;
        this.joueurMapper = joueurMapper;
    }

    @Transactional
    public PlayerResponse create(PlayerCreateRequest req) {
        validateAge(req.dateNaissance());
        joueurRepository.findByNumLicence(req.numLicence()).ifPresent(j -> {
            throw new BusinessException("PLAYER_NUM_LICENSE_EXISTS", "numLicence already exists");
        });
        Joueur j = new Joueur();
        j.setNumLicence(req.numLicence());
        j.setNom(req.nom());
        j.setPrenom(req.prenom());
        j.setDateNaissance(req.dateNaissance());
        j.setPhotoUrl(req.photoUrl());
        j.setEntreprise(req.entreprise());
        Joueur saved = joueurRepository.save(j);
        return joueurMapper.toResponse(saved);
    }

    @Transactional
    public PlayerResponse update(UUID id, PlayerUpdateRequest req) {
        Joueur j = joueurRepository.findById(id).orElseThrow();
        if (req.dateNaissance() != null) validateAge(req.dateNaissance());
        if (req.nom() != null) j.setNom(req.nom());
        if (req.prenom() != null) j.setPrenom(req.prenom());
        if (req.dateNaissance() != null) j.setDateNaissance(req.dateNaissance());
        if (req.photoUrl() != null) j.setPhotoUrl(req.photoUrl());
        if (req.entreprise() != null) j.setEntreprise(req.entreprise());
        return joueurMapper.toResponse(joueurRepository.save(j));
    }

    @Transactional
    public void delete(UUID id) {
        if (equipeRepository.playerInAnyTeam(id)) {
            throw new BusinessException("PLAYER_IN_TEAM", "Cannot delete player assigned to a team");
        }
        joueurRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PlayerResponse get(UUID id) {
        return joueurMapper.toResponse(joueurRepository.findById(id).orElseThrow());
    }

    @Transactional(readOnly = true)
    public Page<PlayerResponse> search(String q, Pageable pageable) {
        return joueurRepository.search(q == null ? "" : q, pageable).map(joueurMapper::toResponse);
    }

    private void validateAge(LocalDate dob) {
        if (dob == null) return;
        if (dob.isAfter(LocalDate.now())) {
            throw new BusinessException("PROFILE_DOB_INVALID", "DOB cannot be in the future");
        }
        int years = Period.between(dob, LocalDate.now()).getYears();
        if (years < 16) throw new BusinessException("PROFILE_DOB_AGE", "Age must be at least 16");
    }
}
