package com.polypadel.test;

import com.polypadel.domain.entity.Utilisateur;
import com.polypadel.domain.enums.Role;
import com.polypadel.users.repository.UtilisateurRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/test")
@Profile("dev")
public class TestUtilsController {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public TestUtilsController(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public static record CreateUserRequest(String email, String password, String role, Boolean active) {}
    public static record UnlockUserRequest(String email) {}

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        Optional<Utilisateur> existing = utilisateurRepository.findByEmail(email);
        Utilisateur user;
        if (existing.isPresent()) {
            user = existing.get();
        } else {
            user = new Utilisateur();
            user.setEmail(email);
            user.setCreatedAt(Instant.now());
        }
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmailHash(hashEmail(email));
        user.setActive(request.active() == null ? true : request.active());
        user.setRole(request.role() == null ? Role.JOUEUR : Role.valueOf(request.role().toUpperCase(Locale.ROOT)));
        utilisateurRepository.save(user);
        return ResponseEntity.ok().body(new java.util.HashMap<String, Object>() {{
            put("id", user.getId());
            put("email", user.getEmail());
            put("role", user.getRole());
        }});
    }

    @PostMapping("/users/unlock")
    public ResponseEntity<?> unlockUser(@RequestBody UnlockUserRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        Optional<Utilisateur> existing = utilisateurRepository.findByEmail(email);
        if (existing.isPresent()) {
            Utilisateur user = existing.get();
            user.setFailedLoginAttempts(0);
            user.setLockoutUntil(null);
            utilisateurRepository.save(user);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    private String hashEmail(String email) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(email.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
