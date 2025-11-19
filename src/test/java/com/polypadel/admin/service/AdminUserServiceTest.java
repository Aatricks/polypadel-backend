package com.polypadel.admin.service;

import com.polypadel.admin.dto.AdminCreateUserRequest;
import com.polypadel.admin.dto.AdminCreateUserResponse;
import com.polypadel.admin.dto.AdminResetPasswordResponse;
import com.polypadel.common.exception.BusinessException;
import com.polypadel.domain.entity.Utilisateur;
import com.polypadel.users.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminUserServiceTest {
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserService adminUserService;

    @Test
    public void create_ok() {
        AdminCreateUserRequest req = new AdminCreateUserRequest("test@ex.com", "Joueur");
        when(utilisateurRepository.findByEmail("test@ex.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(utilisateurRepository.save(any())).thenAnswer(i -> { Utilisateur u = i.getArgument(0); u.setId(UUID.randomUUID()); return u; });

        AdminCreateUserResponse r = adminUserService.create(req);
        assertThat(r.email()).isEqualTo("test@ex.com");
        assertThat(r.tempPassword()).isNotEmpty();
        ArgumentCaptor<Utilisateur> captor = ArgumentCaptor.forClass(Utilisateur.class);
        verify(utilisateurRepository).save(captor.capture());
        assertThat(captor.getValue().getEmailHash()).isNotNull();
    }

    @Test
    public void create_fails_when_email_exists() {
        when(utilisateurRepository.findByEmail("a@b.com")).thenReturn(Optional.of(new Utilisateur()));
        assertThatThrownBy(() -> adminUserService.create(new AdminCreateUserRequest("a@b.com", "ADMIN")))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo("USER_EMAIL_EXISTS"));
    }

    @Test
    public void resetPassword_ok() {
        UUID id = UUID.randomUUID();
        Utilisateur u = new Utilisateur(); u.setId(id);
        when(utilisateurRepository.findById(id)).thenReturn(Optional.of(u));
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(utilisateurRepository.save(any())).thenReturn(u);

        AdminResetPasswordResponse r = adminUserService.resetPassword(id);
        assertThat(r.tempPassword()).isNotEmpty();
    }
}
