package com.polypadel.service;

import com.polypadel.dto.*;
import com.polypadel.model.*;
import com.polypadel.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.security.SecureRandom;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";

    public AdminService(UserRepository userRepository, PlayerRepository playerRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public record CreateAccountResponse(String message, String email, String temporaryPassword, String warning) {}

    public CreateAccountResponse createAccount(Long playerId, String role) {
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

        return new CreateAccountResponse("Compte créé avec succès", user.getEmail(), tempPassword, 
            "Ce mot de passe ne sera affiché qu'une seule fois");
    }

    public CreateAccountResponse resetPassword(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));
        
        String tempPassword = generatePassword();
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        user.setMustChangePassword(true);
        userRepository.save(user);

        return new CreateAccountResponse("Mot de passe réinitialisé", user.getEmail(), tempPassword,
            "Ce mot de passe ne sera affiché qu'une seule fois");
    }

    private String generatePassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
