// removed duplicate top class; tests added to the existing ProfileServiceTest class below
package com.polypadel.users.service;

import com.polypadel.common.exception.BusinessException;
import com.polypadel.domain.entity.Utilisateur;
import com.polypadel.joueurs.repository.JoueurRepository;
import com.polypadel.domain.entity.Joueur;
import com.polypadel.users.dto.PasswordUpdateRequest;
import com.polypadel.users.dto.ProfileResponse;
import com.polypadel.users.dto.ProfileUpdateRequest;
import com.polypadel.users.repository.UtilisateurRepository;
import com.polypadel.domain.enums.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private JoueurRepository joueurRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfileService profileService;

    private UUID userId;

    @BeforeEach
    public void setup() {
        userId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        Mockito.lenient().when(auth.getName()).thenReturn(userId.toString());
        Mockito.lenient().when(auth.getPrincipal()).thenReturn("principal");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void getProfile_without_joueur() {
        Utilisateur u = new Utilisateur(); u.setId(userId); u.setEmail("u@test.com"); u.setRole(Role.JOUEUR);
        when(utilisateurRepository.findById(userId)).thenReturn(Optional.of(u));
        when(joueurRepository.findByUtilisateurId(userId)).thenReturn(Optional.empty());

        ProfileResponse r = profileService.getProfile();
        assertThat(r.email()).isEqualTo("u@test.com");
    }

    @Test
    public void getProfile_fails_without_auth() {
        SecurityContextHolder.clearContext();
        assertThatThrownBy(() -> profileService.getProfile()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void updateProfile_creates_new_joueur_when_missing() {
        Utilisateur u = new Utilisateur(); u.setId(userId); u.setRole(Role.JOUEUR);
        when(utilisateurRepository.findById(userId)).thenReturn(Optional.of(u));
        when(joueurRepository.findByUtilisateurId(userId)).thenReturn(Optional.empty());
        when(joueurRepository.save(any())).thenReturn(new Joueur());

        ProfileUpdateRequest req = new ProfileUpdateRequest("N", "P", LocalDate.now().minusYears(20), null);
        profileService.updateProfile(req);
        verify(joueurRepository).save(any());
    }

    @Test
    public void changePassword_invalid_current_password() {
        Utilisateur u = new Utilisateur(); u.setId(userId); u.setPasswordHash("hash");
        when(utilisateurRepository.findById(userId)).thenReturn(Optional.of(u));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> profileService.changePassword(new PasswordUpdateRequest("wrong", "New1!")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo("PROFILE_PASSWORD_INVALID"));
    }

    @Test
    public void changePassword_weak_new_password() {
        Utilisateur u = new Utilisateur(); u.setId(userId); u.setPasswordHash("hash");
        when(utilisateurRepository.findById(userId)).thenReturn(Optional.of(u));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> profileService.changePassword(new PasswordUpdateRequest("ok", "weak")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo("PROFILE_PASSWORD_WEAK"));
    }

    @Test
    public void changePassword_success() {
        Utilisateur u = new Utilisateur(); u.setId(userId); u.setPasswordHash("hash");
        when(utilisateurRepository.findById(userId)).thenReturn(Optional.of(u));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("newHash");

        profileService.changePassword(new PasswordUpdateRequest("hash", "VeryStr0ng!"));
        verify(utilisateurRepository).save(any());
    }

    @Test
    public void updateProfile_future_date_throws() {
        when(utilisateurRepository.findById(userId)).thenReturn(Optional.of(new Utilisateur()));
        when(joueurRepository.findByUtilisateurId(userId)).thenReturn(Optional.empty());
        ProfileUpdateRequest req = new ProfileUpdateRequest("Name", "Surname", LocalDate.now().plusDays(10), null);
        assertThatThrownBy(() -> profileService.updateProfile(req))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo("PROFILE_DOB_INVALID"));
    }
}
