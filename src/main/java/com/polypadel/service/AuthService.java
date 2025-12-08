package com.polypadel.service;

import com.polypadel.dto.*;
import com.polypadel.model.*;
import com.polypadel.repository.*;
import com.polypadel.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    @Value("${app.lockout.max-attempts}")
    private int maxAttempts;
    
    @Value("${app.lockout.duration-minutes}")
    private int lockoutMinutes;

    public AuthService(UserRepository userRepository, LoginAttemptRepository loginAttemptRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.loginAttemptRepository = loginAttemptRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        LoginAttempt attempt = loginAttemptRepository.findByEmail(request.email())
                .orElseGet(() -> {
                    LoginAttempt newAttempt = new LoginAttempt(request.email());
                    return loginAttemptRepository.save(newAttempt);
                });

        // Check if locked
        if (attempt.getLockedUntil() != null && attempt.getLockedUntil().isAfter(LocalDateTime.now())) {
            long minutesRemaining = ChronoUnit.MINUTES.between(LocalDateTime.now(), attempt.getLockedUntil()) + 1;
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "Compte bloqué. Réessayez dans " + minutesRemaining + " minutes");
        }

        User user = userRepository.findByEmail(request.email()).orElse(null);
        
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            // Increment failed attempts
            attempt.setAttemptsCount(attempt.getAttemptsCount() + 1);
            attempt.setLastAttempt(LocalDateTime.now());
            
            if (attempt.getAttemptsCount() >= maxAttempts) {
                attempt.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutMinutes));
                loginAttemptRepository.save(attempt);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Compte bloqué pendant " + lockoutMinutes + " minutes après " + maxAttempts + " tentatives échouées");
            }
            
            loginAttemptRepository.save(attempt);
            int remaining = maxAttempts - attempt.getAttemptsCount();
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                "Email ou mot de passe incorrect. " + remaining + " tentative(s) restante(s)");
        }

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Compte désactivé");
        }

        // Reset attempts on success
        attempt.setAttemptsCount(0);
        attempt.setLockedUntil(null);
        loginAttemptRepository.save(attempt);

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new LoginResponse(token, "bearer", 
            new LoginResponse.UserDto(user.getId(), user.getEmail(), user.getRole().name(), user.isMustChangePassword()));
    }

    public void changePassword(User user, PasswordChangeRequest request) {
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mot de passe actuel incorrect");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Les mots de passe ne correspondent pas");
        }
        if (request.newPassword().equals(request.currentPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nouveau mot de passe doit être différent");
        }
        if (!isPasswordStrong(request.newPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Le mot de passe doit contenir au moins 12 caractères avec majuscules, minuscules, chiffres et caractères spéciaux");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    private boolean isPasswordStrong(String password) {
        return password.length() >= 12 &&
               password.chars().anyMatch(Character::isUpperCase) &&
               password.chars().anyMatch(Character::isLowerCase) &&
               password.chars().anyMatch(Character::isDigit) &&
               password.chars().anyMatch(c -> "!@#$%^&*()_+-=[]{}|;':\",./<>?".indexOf(c) >= 0);
    }
}
