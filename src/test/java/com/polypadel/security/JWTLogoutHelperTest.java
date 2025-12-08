package com.polypadel.security;

import com.polypadel.auth.repository.JSONTokenRepository;
import com.polypadel.domain.entity.JSONToken;
import com.polypadel.domain.entity.Utilisateur;
import com.polypadel.users.repository.UtilisateurRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JWTLogoutHelperTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private JSONTokenRepository jsonTokenRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    private JWTLogoutHelper helper;
    @org.junit.jupiter.api.BeforeEach
    public void setUp() {
        helper = new JWTLogoutHelper(jwtService, jsonTokenRepository, utilisateurRepository);
    }
    @Test
    public void logout_with_cookie_revokes_token_and_clears_cookie() {
        String jti = UUID.randomUUID().toString();
        UUID userId = UUID.randomUUID();
        Claims claims = Jwts.claims(); claims.setId(jti); claims.setSubject(userId.toString()); claims.setExpiration(java.util.Date.from(Instant.now().plusSeconds(3600)));
        when(jwtService.parse(any())).thenReturn(claims);
        Utilisateur u = new Utilisateur(); u.setId(userId);
        when(utilisateurRepository.findById(userId)).thenReturn(Optional.of(u));

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        Cookie c = new Cookie("JWT", "tok");
        when(req.getCookies()).thenReturn(new Cookie[]{c});

        helper.logout(req, resp);

        ArgumentCaptor<JSONToken> captor = ArgumentCaptor.forClass(JSONToken.class);
        verify(jsonTokenRepository).save(captor.capture());
        JSONToken saved = captor.getValue();
        assertThat(saved.getJti()).isEqualTo(jti);
        verify(resp).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    public void logout_with_authorization_header_revokes_token_and_clears_cookie() {
        String jti = UUID.randomUUID().toString();
        UUID userId = UUID.randomUUID();
        Claims claims = Jwts.claims(); claims.setId(jti); claims.setSubject(userId.toString()); claims.setExpiration(java.util.Date.from(Instant.now().plusSeconds(3600)));
        when(jwtService.parse(any())).thenReturn(claims);
        Utilisateur u = new Utilisateur(); u.setId(userId);
        when(utilisateurRepository.findById(userId)).thenReturn(Optional.of(u));

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getCookies()).thenReturn(null);
        when(req.getHeader(org.springframework.http.HttpHeaders.AUTHORIZATION)).thenReturn("Bearer tokheader");

        helper.logout(req, resp);
        ArgumentCaptor<JSONToken> captor2 = ArgumentCaptor.forClass(JSONToken.class);
        verify(jsonTokenRepository).save(captor2.capture());
        verify(resp).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    public void logout_with_invalid_token_still_clears_cookie() {
        when(jwtService.parse(any())).thenThrow(new RuntimeException("bad"));
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        Cookie c = new Cookie("JWT", "bad");
        when(req.getCookies()).thenReturn(new Cookie[]{c});

        helper.logout(req, resp);
        verifyNoInteractions(jsonTokenRepository);
        verify(resp).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    public void logout_with_no_token_clears_cookie() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getCookies()).thenReturn(null);
        when(req.getHeader(org.springframework.http.HttpHeaders.AUTHORIZATION)).thenReturn(null);

        helper.logout(req, resp);
        verifyNoInteractions(jsonTokenRepository);
        verify(resp).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    public void logout_with_valid_token_but_user_not_found_does_not_save_token() {
        String jti = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        Claims claims = Jwts.claims(); claims.setId(jti); claims.setSubject(userId); claims.setExpiration(java.util.Date.from(Instant.now().plusSeconds(3600)));
        when(jwtService.parse(any())).thenReturn(claims);
        when(utilisateurRepository.findById(UUID.fromString(userId))).thenReturn(Optional.empty());

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        Cookie c = new Cookie("JWT", "tok");
        when(req.getCookies()).thenReturn(new Cookie[]{c});

        helper.logout(req, resp);
        verifyNoInteractions(jsonTokenRepository);
        verify(resp).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    public void logout_jti_null_does_not_save_token() {
        String userId = UUID.randomUUID().toString();
        Claims claims = Jwts.claims(); claims.setId(null); claims.setSubject(userId); claims.setExpiration(java.util.Date.from(Instant.now().plusSeconds(3600)));
        when(jwtService.parse(any())).thenReturn(claims);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        Cookie c = new Cookie("JWT", "tok");
        when(req.getCookies()).thenReturn(new Cookie[]{c});

        helper.logout(req, resp);
        verifyNoInteractions(jsonTokenRepository);
        verify(resp).addHeader(eq("Set-Cookie"), anyString());
    }
}
