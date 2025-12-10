package com.polypadel;

import com.polypadel.dto.LoginRequest;
import com.polypadel.dto.PasswordChangeRequest;
import com.polypadel.model.LoginAttempt;
import com.polypadel.model.Role;
import com.polypadel.model.User;
import com.polypadel.repository.LoginAttemptRepository;
import com.polypadel.repository.UserRepository;
import com.polypadel.security.JwtService;
import com.polypadel.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {

    @Mock private UserRepository userRepository;
    @Mock private LoginAttemptRepository loginAttemptRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @InjectMocks private AuthService authService;

    @Test
    void changePasswordUpdatesHash() {
        ReflectionTestUtils.setField(authService, "maxAttempts", 3);
        ReflectionTestUtils.setField(authService, "lockoutMinutes", 5);

        User user = new User("user@test.com", "old-hash", Role.JOUEUR);
        when(passwordEncoder.matches("current", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("StrongP@ssw0rd!")).thenReturn("new-hash");
        when(userRepository.save(user)).thenReturn(user);

        authService.changePassword(user, new PasswordChangeRequest("current", "StrongP@ssw0rd!", "StrongP@ssw0rd!"));

        assertEquals("new-hash", user.getPasswordHash());
        assertFalse(user.isMustChangePassword());
        verify(userRepository).save(user);
    }

    @Test
    void changePasswordValidations() {
        User user = new User("user@test.com", "old-hash", Role.JOUEUR);
        when(passwordEncoder.matches("bad", "old-hash")).thenReturn(false);

        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class, () ->
            authService.changePassword(user, new PasswordChangeRequest("bad", "NewPassword1!", "NewPassword1!"))
        ).getStatusCode());

        when(passwordEncoder.matches("current", "old-hash")).thenReturn(true);
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class, () ->
            authService.changePassword(user, new PasswordChangeRequest("current", "NewPassword1!", "Mismatch"))
        ).getStatusCode());

        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class, () ->
            authService.changePassword(user, new PasswordChangeRequest("current", "weakpass", "weakpass"))
        ).getStatusCode());
    }

    @Test
    void loginRespectsLockout() {
        LoginAttempt attempt = new LoginAttempt("locked@test.com");
        attempt.setLockedUntil(LocalDateTime.now().plusMinutes(10));

        when(loginAttemptRepository.findByEmail("locked@test.com")).thenReturn(Optional.of(attempt));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> authService.login(new LoginRequest("locked@test.com", "ignored")));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }
}
