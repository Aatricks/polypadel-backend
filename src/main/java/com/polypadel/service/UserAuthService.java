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
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Unified service for authentication and user account management.
 * Combines former AuthService and AdminService functionality.
 */
@Service
public class UserAuthService {
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;':\",./<>?]).{12,}$";

    @Value("${app.lockout.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.lockout.duration-minutes:15}")
    private int lockoutMinutes;

    public UserAuthService(UserRepository userRepository, PlayerRepository playerRepository,
                           LoginAttemptRepository loginAttemptRepository, PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.loginAttemptRepository = loginAttemptRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // ========== Authentication ==========

    public LoginResponse login(LoginRequest request) {
        LoginAttempt attempt = loginAttemptRepository.findByEmail(request.email())
                .orElseGet(() -> loginAttemptRepository.save(new LoginAttempt(request.email())));

        // Check lockout
        if (attempt.getLockedUntil() != null && attempt.getLockedUntil().isAfter(LocalDateTime.now())) {
            long minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), attempt.getLockedUntil()) + 1;
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Compte bloqué. Réessayez dans " + minutes + " minutes");
        }

        User user = userRepository.findByEmail(request.email()).orElse(null);

        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            attempt.setAttemptsCount(attempt.getAttemptsCount() + 1);
            attempt.setLastAttempt(LocalDateTime.now());

            if (attempt.getAttemptsCount() >= maxAttempts) {
                attempt.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutMinutes));
                loginAttemptRepository.save(attempt);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Compte bloqué pendant " + lockoutMinutes + " minutes après " + maxAttempts + " tentatives échouées");
            }

            loginAttemptRepository.save(attempt);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Email ou mot de passe incorrect. " + (maxAttempts - attempt.getAttemptsCount()) + " tentative(s) restante(s)");
        }

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Compte désactivé");
        }

        // Reset on success
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
        if (!request.newPassword().matches(PASSWORD_PATTERN)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Le mot de passe doit contenir au moins 12 caractères avec majuscules, minuscules, chiffres et caractères spéciaux");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    // ========== Admin Account Management ==========

    public record AccountResponse(String message, String email, String temporaryPassword, String warning) {}

    public AccountResponse createAccount(Long playerId, String role) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Joueur non trouvé"));

        if (player.getUser() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce joueur a déjà un compte");
        }

        String tempPassword = generatePassword();
        User user = new User();
        user.setEmail(player.getLicenseNumber() + "@polypadel.local");
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        user.setRole(role != null ? Role.valueOf(role) : Role.JOUEUR);
        user.setMustChangePassword(true);
        user = userRepository.save(user);

        player.setUser(user);
        playerRepository.save(player);

        return new AccountResponse("Compte créé avec succès", user.getEmail(), tempPassword,
            "Ce mot de passe ne sera affiché qu'une seule fois");
    }

    public AccountResponse resetPassword(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

        String tempPassword = generatePassword();
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        user.setMustChangePassword(true);
        userRepository.save(user);

        return new AccountResponse("Mot de passe réinitialisé", user.getEmail(), tempPassword,
            "Ce mot de passe ne sera affiché qu'une seule fois");
    }

    private String generatePassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
