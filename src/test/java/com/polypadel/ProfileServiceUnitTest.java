package com.polypadel;

import com.polypadel.dto.ProfileUpdateRequest;
import com.polypadel.model.Player;
import com.polypadel.model.Role;
import com.polypadel.model.User;
import com.polypadel.repository.PlayerRepository;
import com.polypadel.repository.UserRepository;
import com.polypadel.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceUnitTest {

    @Mock private UserRepository userRepository;
    @Mock private PlayerRepository playerRepository;
    @InjectMocks private ProfileService profileService;

    @TempDir
    Path tempDir;

    @Test
    void updateProfileChangesValuesAndEmail() {
        User user = new User("old@mail.com", "hash", Role.JOUEUR);
        user.setId(1L);
        Player player = new Player();
        player.setId(2L);
        player.setUser(user);

        when(playerRepository.findByUserId(1L)).thenReturn(Optional.of(player));
        when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(playerRepository.save(player)).thenReturn(player);

        ProfileUpdateRequest request = new ProfileUpdateRequest("NewFirst", "NewLast",
            LocalDate.of(1990, 1, 1), "new@mail.com");

        var response = profileService.updateProfile(user, request);

        assertEquals("new@mail.com", user.getEmail());
        assertEquals("NewFirst", player.getFirstName());
        assertEquals("NewLast", player.getLastName());
        assertEquals(LocalDate.of(1990, 1, 1), player.getBirthDate());
        assertEquals("new@mail.com", response.user().email());
        verify(userRepository).save(user);
        verify(playerRepository).save(player);
    }

    @Test
    void updateProfileRejectsDuplicateEmail() {
        User user = new User("old@mail.com", "hash", Role.JOUEUR);
        user.setId(3L);
        Player player = new Player();
        player.setId(4L);
        player.setUser(user);

        when(playerRepository.findByUserId(3L)).thenReturn(Optional.of(player));
        when(userRepository.existsByEmail("dup@mail.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            profileService.updateProfile(user,
                new ProfileUpdateRequest(null, null, null, "dup@mail.com")));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void uploadPhotoStoresFileAndUpdatesPlayer() throws IOException {
        ReflectionTestUtils.setField(profileService, "uploadDir", tempDir.toString());

        User user = new User("photo@mail.com", "hash", Role.JOUEUR);
        user.setId(5L);
        Player player = new Player();
        player.setId(6L);
        player.setUser(user);

        when(playerRepository.findByUserId(5L)).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile photo = new MockMultipartFile("file", "photo.png", "image/png", "img".getBytes());

        String url = profileService.uploadPhoto(user, photo);

        assertTrue(url.startsWith("/uploads/"));
        Path stored = tempDir.resolve(url.replace("/uploads/", ""));
        assertTrue(Files.exists(stored));
        assertEquals(url, player.getPhotoUrl());
        verify(playerRepository, times(1)).save(any(Player.class));
    }

    @Test
    void uploadPhotoRejectsInvalidInput() {
        ReflectionTestUtils.setField(profileService, "uploadDir", tempDir.toString());

        User user = new User("invalid@mail.com", "hash", Role.JOUEUR);
        user.setId(7L);
        Player player = new Player();
        player.setId(8L);
        when(playerRepository.findByUserId(7L)).thenReturn(Optional.of(player));

        MockMultipartFile empty = new MockMultipartFile("file", new byte[0]);
        assertThrows(ResponseStatusException.class, () -> profileService.uploadPhoto(user, empty));

        MockMultipartFile badType = new MockMultipartFile("file", "x.txt", "text/plain", "data".getBytes());
        assertThrows(ResponseStatusException.class, () -> profileService.uploadPhoto(user, badType));
    }

    @Test
    void deletePhotoRemovesFile() throws IOException {
        ReflectionTestUtils.setField(profileService, "uploadDir", tempDir.toString());

        User user = new User("delete@mail.com", "hash", Role.JOUEUR);
        user.setId(9L);
        Player player = new Player();
        player.setId(10L);
        player.setUser(user);

        Path existing = tempDir.resolve("old.png");
        Files.writeString(existing, "old");
        player.setPhotoUrl("/uploads/old.png");

        when(playerRepository.findByUserId(9L)).thenReturn(Optional.of(player));
        when(playerRepository.save(eq(player))).thenReturn(player);

        profileService.deletePhoto(user);

        assertNull(player.getPhotoUrl());
        assertFalse(Files.exists(existing));
        verify(playerRepository).save(player);
    }
}
