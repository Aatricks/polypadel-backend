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
import java.time.Duration;

@Service
public class AuthService {

    private final int maxFailedAttempts;
    private final Duration lockoutDuration;

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UtilisateurRepository utilisateurRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       @org.springframework.beans.factory.annotation.Value("${security.lockout.maxAttempts:3}") int maxFailedAttempts,
                       @org.springframework.beans.factory.annotation.Value("${security.lockout.durationMinutes:30}") long lockoutDurationMinutes) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.maxFailedAttempts = maxFailedAttempts;
        this.lockoutDuration = Duration.ofMinutes(lockoutDurationMinutes);
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
        // If the user is currently locked out (brute-force protection)
        if (user.getLockoutUntil() != null) {
            if (user.getLockoutUntil().isAfter(Instant.now())) {
                long minutesRemaining = Duration.between(Instant.now(), user.getLockoutUntil()).toMinutes();
                throw new com.polypadel.common.exception.AuthException(ErrorCodes.AUTH_ACCOUNT_LOCKED, "Account locked until " + user.getLockoutUntil(), 0, minutesRemaining);
            } else {
                // Lockout expired - reset counters
                user.setFailedLoginAttempts(0);
                user.setLockoutUntil(null);
                utilisateurRepository.save(user);
            }
        }

        // Bad password: increment counter and lock if threshold reached
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= this.maxFailedAttempts) {
                user.setLockoutUntil(Instant.now().plus(this.lockoutDuration));
                utilisateurRepository.save(user);
                long minutesRemaining = this.lockoutDuration.toMinutes();
                throw new com.polypadel.common.exception.AuthException(ErrorCodes.AUTH_ACCOUNT_LOCKED, "Account locked due to too many failed login attempts", 0, minutesRemaining);
            } else {
                utilisateurRepository.save(user);
                int attemptsRemaining = this.maxFailedAttempts - attempts;
                throw new com.polypadel.common.exception.AuthException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "Invalid credentials", attemptsRemaining, null);
            }
        }

        // Successful login: reset counters and update last login
        user.setFailedLoginAttempts(0);
        user.setLockoutUntil(null);
        user.setLastLoginAt(Instant.now());
        utilisateurRepository.save(user);
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
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
