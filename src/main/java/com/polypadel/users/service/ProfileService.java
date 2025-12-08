package com.polypadel.users.service;

import com.polypadel.common.exception.BusinessException;
import com.polypadel.common.exception.ErrorCodes;
import com.polypadel.common.exception.NotFoundException;
import com.polypadel.domain.entity.Joueur;
import com.polypadel.domain.entity.Utilisateur;
import com.polypadel.joueurs.repository.JoueurRepository;
import com.polypadel.users.repository.UtilisateurRepository;
import com.polypadel.users.dto.PasswordUpdateRequest;
import com.polypadel.users.dto.ProfileResponse;
import com.polypadel.users.dto.ProfileUpdateRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

@Service
public class ProfileService {

    private final UtilisateurRepository utilisateurRepository;
    private final JoueurRepository joueurRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(UtilisateurRepository utilisateurRepository, JoueurRepository joueurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.joueurRepository = joueurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private UUID currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) throw new IllegalStateException("No auth");
        return UUID.fromString(auth.getName());
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile() {
        UUID id = currentUserId();
        Utilisateur u = utilisateurRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.USER_NOT_FOUND, "User not found: " + id));
        
        return joueurRepository.findByUtilisateurId(id)
            .map(j -> new ProfileResponse(
                u.getId(),
                u.getEmail(),
                u.getRole().name(),
                j.getId(),
                j.getNom(),
                j.getPrenom(),
                j.getDateNaissance(),
                j.getPhotoUrl(),
                j.getEntreprise()
            ))
            .orElseGet(() -> new ProfileResponse(
                u.getId(),
                u.getEmail(),
                u.getRole().name(),
                null, null, null, null, null, null
            ));
    }

    @Transactional
    public ProfileResponse updateProfile(ProfileUpdateRequest req) {
        UUID id = currentUserId();
        Joueur j = joueurRepository.findByUtilisateurId(id).orElseGet(() -> {
            Joueur nj = new Joueur();
            nj.setUtilisateur(utilisateurRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.USER_NOT_FOUND, "User not found: " + id)));
            // entreprise cannot be set here; keep null; admin flow sets it
            nj.setEntreprise("UNSET");
            nj.setNumLicence(UUID.randomUUID().toString());
            return nj;
        });
        if (req.dateNaissance() != null) {
            validateAge(req.dateNaissance());
        }
        j.setNom(req.nom());
        j.setPrenom(req.prenom());
        j.setDateNaissance(req.dateNaissance());
        j.setPhotoUrl(req.photoUrl());
        joueurRepository.save(j);
        return getProfile();
    }

    @Transactional
    public void changePassword(PasswordUpdateRequest req) {
        UUID id = currentUserId();
        Utilisateur u = utilisateurRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.USER_NOT_FOUND, "User not found: " + id));
        if (!passwordEncoder.matches(req.currentPassword(), u.getPasswordHash())) {
            throw new BusinessException("PROFILE_PASSWORD_INVALID", "Current password invalid");
        }
        if (!isStrongPassword(req.newPassword())) {
            throw new BusinessException("PROFILE_PASSWORD_WEAK", "New password weak");
        }
        u.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        utilisateurRepository.save(u);
    }

    private void validateAge(LocalDate dob) {
        if (dob.isAfter(LocalDate.now())) {
            throw new BusinessException("PROFILE_DOB_INVALID", "DOB cannot be in the future");
        }
        int years = Period.between(dob, LocalDate.now()).getYears();
        if (years < 16) {
            throw new BusinessException("PROFILE_DOB_AGE", "Age must be at least 16");
        }
    }

    private boolean isStrongPassword(String s) {
        return s != null && s.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");
    }
}
