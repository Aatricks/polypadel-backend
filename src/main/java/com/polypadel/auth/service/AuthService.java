package com.polypadel.auth.service;

import com.polypadel.auth.dto.LoginResponse;
import com.polypadel.auth.dto.RegisterRequest;
import com.polypadel.auth.dto.UserSummary;
import com.polypadel.common.exception.BusinessException;
import com.polypadel.common.exception.ErrorCodes;
import com.polypadel.domain.entity.Utilisateur;
import com.polypadel.domain.enums.Role;
import com.polypadel.users.repository.UtilisateurRepository;
import com.polypadel.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

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

    public LoginResponse register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BusinessException(ErrorCodes.AUTH_PASSWORDS_DO_NOT_MATCH, "Passwords do not match");
        }

        if (utilisateurRepository.findByEmail(request.email().trim().toLowerCase()).isPresent()) {
            throw new BusinessException(ErrorCodes.AUTH_EMAIL_ALREADY_EXISTS, "Email already exists");
        }

        Utilisateur user = new Utilisateur();
        user.setEmail(request.email().trim().toLowerCase());
        user.setEmailHash(hashEmail(request.email().trim().toLowerCase()));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.JOUEUR); // Default role
        user.setActive(true);
        user.setCreatedAt(Instant.now());
        utilisateurRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new LoginResponse(token, new UserSummary(user.getId(), user.getEmail(), user.getRole().name()));
    }

    private String hashEmail(String email) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(email.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
