package com.polypadel.auth.service;

import com.polypadel.auth.dto.LoginResponse;
import com.polypadel.auth.dto.UserSummary;
import com.polypadel.common.exception.BusinessException;
import com.polypadel.domain.entity.Utilisateur;
import com.polypadel.domain.enums.Role;
import com.polypadel.users.repository.UtilisateurRepository;
import com.polypadel.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;
import java.time.Instant;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

public class AuthServiceTest {

    private UtilisateurRepository utilisateurRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        utilisateurRepository = Mockito.mock(UtilisateurRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        jwtService = Mockito.mock(JwtService.class);
        authService = new AuthService(utilisateurRepository, passwordEncoder, jwtService, 3, 30);
    }

    @Test
    void login_successful_returns_token_and_summary() {
        Utilisateur u = new Utilisateur();
        u.setId(UUID.randomUUID());
        u.setEmail("test@example.com");
        u.setPasswordHash("hash");
        u.setRole(Role.JOUEUR);
        u.setActive(true);
        Mockito.when(utilisateurRepository.findByEmail(any(String.class))).thenReturn(Optional.of(u));
        Mockito.when(passwordEncoder.matches(any(String.class), any(String.class))).thenReturn(true);
        Mockito.when(jwtService.generateToken(any(UUID.class), any(String.class), any(Role.class))).thenReturn("token");
        Mockito.when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(u);

        LoginResponse resp = authService.login("test@example.com", "pw");
        assertEquals("token", resp.token());
        assertEquals(u.getEmail(), resp.user().email());
        assertEquals(u.getId(), resp.user().id());
    }

    @Test
    void login_user_not_found_throws() {
        Mockito.when(utilisateurRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());
        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login("x", "y"));
        assertEquals("AUTH_INVALID_CREDENTIALS", ex.getCode());
    }

    @Test
    void login_inactive_throws() {
        Utilisateur u = new Utilisateur();
        u.setActive(false);
        Mockito.when(utilisateurRepository.findByEmail(any(String.class))).thenReturn(Optional.of(u));
        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login("x", "y"));
        assertEquals("AUTH_INACTIVE_ACCOUNT", ex.getCode());
    }

    @Test
    void login_missing_role_throws() {
        Utilisateur u = new Utilisateur();
        u.setActive(true);
        u.setRole(null);
        Mockito.when(utilisateurRepository.findByEmail(any(String.class))).thenReturn(Optional.of(u));
        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login("x", "y"));
        assertEquals("AUTH_MISSING_ROLE", ex.getCode());
    }

    @Test
    void login_wrong_password_throws() {
        Utilisateur u = new Utilisateur();
        u.setActive(true);
        u.setRole(Role.ADMIN);
        u.setPasswordHash("hash");
        Mockito.when(utilisateurRepository.findByEmail(any(String.class))).thenReturn(Optional.of(u));
        Mockito.when(passwordEncoder.matches(any(String.class), any(String.class))).thenReturn(false);
        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login("x", "y"));
        assertEquals("AUTH_INVALID_CREDENTIALS", ex.getCode());
    }

    @Test
    void login_increments_failed_attempts_and_locks_after_three() {
        Utilisateur u = new Utilisateur();
        u.setActive(true);
        u.setRole(Role.ADMIN);
        u.setPasswordHash("hash");
        Mockito.when(utilisateurRepository.findByEmail(any(String.class))).thenReturn(Optional.of(u));
        Mockito.when(passwordEncoder.matches(any(String.class), any(String.class))).thenReturn(false);
        Mockito.when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 1st failed attempt
        BusinessException ex1 = assertThrows(BusinessException.class, () -> authService.login("x", "y"));
        assertEquals("AUTH_INVALID_CREDENTIALS", ex1.getCode());
        assertEquals(1, u.getFailedLoginAttempts());

        // 2nd failed attempt
        BusinessException ex2 = assertThrows(BusinessException.class, () -> authService.login("x", "y"));
        assertEquals("AUTH_INVALID_CREDENTIALS", ex2.getCode());
        assertEquals(2, u.getFailedLoginAttempts());

        // 3rd failed attempt -> account locked
        BusinessException ex3 = assertThrows(BusinessException.class, () -> authService.login("x", "y"));
        assertEquals("AUTH_ACCOUNT_LOCKED", ex3.getCode());
        assertEquals(3, u.getFailedLoginAttempts());
        assertNotNull(u.getLockoutUntil());
        assertTrue(u.getLockoutUntil().isAfter(Instant.now()));

        // Subsequent attempt while locked should fail with locked error
        BusinessException ex4 = assertThrows(BusinessException.class, () -> authService.login("x", "y"));
        assertEquals("AUTH_ACCOUNT_LOCKED", ex4.getCode());
    }

    @Test
    void login_after_lockout_expiry_resets_attempts_and_allows_login() {
        Utilisateur u = new Utilisateur();
        u.setActive(true);
        u.setRole(Role.ADMIN);
        u.setPasswordHash("hash");
        u.setId(UUID.randomUUID());
        u.setEmail("test@example.com");
        // Simulate previous lockout expired
        u.setFailedLoginAttempts(3);
        u.setLockoutUntil(Instant.now().minus(Duration.ofMinutes(1)));
        Mockito.when(utilisateurRepository.findByEmail(any(String.class))).thenReturn(Optional.of(u));
        Mockito.when(passwordEncoder.matches(any(String.class), any(String.class))).thenReturn(true);
        Mockito.when(jwtService.generateToken(any(UUID.class), any(String.class), any(Role.class))).thenReturn("token");
        Mockito.when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoginResponse resp = authService.login("test@example.com", "pw");
        assertEquals("token", resp.token());
        assertEquals(0, u.getFailedLoginAttempts());
        assertNull(u.getLockoutUntil());
    }

    @Test
    void login_success_resets_failed_attempts() {
        Utilisateur u = new Utilisateur();
        u.setActive(true);
        u.setRole(Role.ADMIN);
        u.setPasswordHash("hash");
        u.setFailedLoginAttempts(2);
        u.setId(UUID.randomUUID());
        u.setEmail("test@example.com");
        Mockito.when(utilisateurRepository.findByEmail(any(String.class))).thenReturn(Optional.of(u));
        Mockito.when(passwordEncoder.matches(any(String.class), any(String.class))).thenReturn(true);
        Mockito.when(jwtService.generateToken(any(UUID.class), any(String.class), any(Role.class))).thenReturn("token");
        Mockito.when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoginResponse resp = authService.login("test@example.com", "pw");
        assertEquals("token", resp.token());
        assertEquals(0, u.getFailedLoginAttempts());
        assertNull(u.getLockoutUntil());
    }

    @Test
    void login_visiteur_role_throws() {
        Utilisateur u = new Utilisateur();
        u.setActive(true);
        u.setRole(Role.VISITEUR);
        Mockito.when(utilisateurRepository.findByEmail(any(String.class))).thenReturn(Optional.of(u));
        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login("x", "y"));
        assertEquals("AUTH_MISSING_ROLE", ex.getCode());
    }

    @Test
    void register_successful_returns_token() {
        com.polypadel.auth.dto.RegisterRequest req = new com.polypadel.auth.dto.RegisterRequest(
            "new@example.com", "ValidP@ss1", "ValidP@ss1"
        );
        Mockito.when(utilisateurRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());
        Mockito.when(passwordEncoder.encode(any(String.class))).thenReturn("encodedHash");
        Mockito.when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> {
            Utilisateur saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });
        Mockito.when(jwtService.generateToken(any(UUID.class), any(String.class), any(Role.class))).thenReturn("newtoken");

        LoginResponse resp = authService.register(req);
        assertEquals("newtoken", resp.token());
        assertEquals("new@example.com", resp.user().email());
    }

    @Test
    void register_passwords_do_not_match_throws() {
        com.polypadel.auth.dto.RegisterRequest req = new com.polypadel.auth.dto.RegisterRequest(
            "new@example.com", "ValidP@ss1", "Different"
        );
        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(req));
        assertEquals("AUTH_PASSWORDS_DO_NOT_MATCH", ex.getCode());
    }

    @Test
    void register_email_already_exists_throws() {
        com.polypadel.auth.dto.RegisterRequest req = new com.polypadel.auth.dto.RegisterRequest(
            "existing@example.com", "ValidP@ss1", "ValidP@ss1"
        );
        Utilisateur existing = new Utilisateur();
        existing.setEmail("existing@example.com");
        Mockito.when(utilisateurRepository.findByEmail(any(String.class))).thenReturn(Optional.of(existing));
        
        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(req));
        assertEquals("AUTH_EMAIL_ALREADY_EXISTS", ex.getCode());
    }
    
}
