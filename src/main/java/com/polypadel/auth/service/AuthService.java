package com.polypadel.auth.service;

import com.polypadel.auth.dto.LoginResponse;
import com.polypadel.auth.dto.UserSummary;
import com.polypadel.common.exception.BusinessException;
import com.polypadel.common.exception.ErrorCodes;
import com.polypadel.domain.entity.Utilisateur;
import com.polypadel.domain.enums.Role;
import com.polypadel.users.repository.UtilisateurRepository;
import com.polypadel.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(String email, String password) {
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "Invalid credentials"));

        if (!user.isActive()) {
            throw new BusinessException(ErrorCodes.AUTH_INACTIVE_ACCOUNT, "Account inactive");
        }
        if (user.getRole() == null || user.getRole() == Role.VISITEUR) {
            throw new BusinessException(ErrorCodes.AUTH_MISSING_ROLE, "Missing role");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        user.setLastLoginAt(Instant.now());
        utilisateurRepository.save(user);
        return new LoginResponse(token, new UserSummary(user.getId(), user.getEmail(), user.getRole().name()));
    }
}
