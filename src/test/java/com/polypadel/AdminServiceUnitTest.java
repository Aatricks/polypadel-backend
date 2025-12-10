package com.polypadel;

import com.polypadel.model.Player;
import com.polypadel.model.Role;
import com.polypadel.model.User;
import com.polypadel.repository.PlayerRepository;
import com.polypadel.repository.UserRepository;
import com.polypadel.service.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceUnitTest {

    @Mock private UserRepository userRepository;
    @Mock private PlayerRepository playerRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private AdminService adminService;

    @Test
    void createAccountGeneratesCredentials() {
        Player player = new Player();
        player.setId(1L);
        player.setLicenseNumber("LIC123");

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(99L);
            return u;
        });
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminService.CreateAccountResponse response = adminService.createAccount(1L, "ADMINISTRATEUR");

        assertEquals("LIC123@polypadel.local", response.email());
        assertTrue(response.temporaryPassword().length() >= 16);
        assertEquals("Compte créé avec succès", response.message());
        assertNotNull(player.getUser());
        assertTrue(player.getUser().isMustChangePassword());
        verify(userRepository).save(any(User.class));
        verify(playerRepository).save(player);
    }

    @Test
    void createAccountFailsWhenAlreadyLinked() {
        Player player = new Player();
        player.setId(2L);
        player.setUser(new User("x@test.com", "hash", Role.JOUEUR));

        when(playerRepository.findById(2L)).thenReturn(Optional.of(player));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> adminService.createAccount(2L, null));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void resetPasswordUpdatesHash() {
        User user = new User("reset@test.com", "old", Role.JOUEUR);
        user.setId(3L);

        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(any())).thenReturn("new-hash");
        when(userRepository.save(eq(user))).thenReturn(user);

        AdminService.CreateAccountResponse response = adminService.resetPassword(3L);

        assertEquals("new-hash", user.getPasswordHash());
        assertTrue(user.isMustChangePassword());
        assertEquals("reset@test.com", response.email());
    }

    @Test
    void resetPasswordNotFound() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> adminService.resetPassword(404L));
    }
}
