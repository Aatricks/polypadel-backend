package com.polypadel.security;

import com.polypadel.auth.repository.JSONTokenRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private JSONTokenRepository jsonTokenRepository;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    public void setup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void doFilterInternal_sets_authentication_from_authorization_header() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("jti-1");
        when(claims.getSubject()).thenReturn("user1");
        when(claims.get("role", String.class)).thenReturn("ADMIN");
        when(jwtService.parse("token123")).thenReturn(claims);
        when(jsonTokenRepository.existsByJtiAndRevokedTrue("jti-1")).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities()).isNotEmpty();
        assertThat(auth.getName()).isEqualTo("user1");
    }

    @Test
    public void doFilterInternal_sets_authentication_from_cookie() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("JWT", "token456"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("jti-2");
        when(claims.getSubject()).thenReturn("user2");
        when(claims.get("role", String.class)).thenReturn("JOUEUR");
        when(jwtService.parse("token456")).thenReturn(claims);
        when(jsonTokenRepository.existsByJtiAndRevokedTrue("jti-2")).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities()).isNotEmpty();
        assertThat(auth.getName()).isEqualTo("user2");
    }

    @Test
    public void doFilterInternal_skips_revoked_token() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer revoked");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("jti-revoked");
        when(jwtService.parse("revoked")).thenReturn(claims);
        when(jsonTokenRepository.existsByJtiAndRevokedTrue("jti-revoked")).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();
    }

    @Test
    public void doFilterInternal_handles_parse_exception_gracefully() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.parse("invalid")).thenThrow(new RuntimeException("invalid"));

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();
    }

    @Test
    public void doFilterInternal_no_token_sets_no_authentication() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();
    }

    @Test
    public void doFilterInternal_header_without_bearer_ignored() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "token123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();
    }

    @Test
    public void doFilterInternal_jti_null_skips_revoked_check() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer tokenX");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn(null);
        when(claims.getSubject()).thenReturn("userX");
        when(claims.get("role", String.class)).thenReturn("JOUEUR");
        when(jwtService.parse("tokenX")).thenReturn(claims);

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        verify(jsonTokenRepository, never()).existsByJtiAndRevokedTrue(anyString());
    }
}
