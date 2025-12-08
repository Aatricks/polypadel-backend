package com.polypadel.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    public void setup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void doFilterInternal_sets_auth_from_header() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token123");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user1");
        when(claims.get("role", String.class)).thenReturn("ADMIN");
        when(jwtService.parse("token123")).thenReturn(claims);

        filter.doFilterInternal(request, response, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("user1");
    }

    @Test
    public void doFilterInternal_sets_auth_from_cookie() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        request.setCookies(new Cookie("JWT", "token456"));
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user2");
        when(claims.get("role", String.class)).thenReturn("JOUEUR");
        when(jwtService.parse("token456")).thenReturn(claims);

        filter.doFilterInternal(request, response, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("user2");
    }

    @Test
    public void doFilterInternal_handles_exception() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        when(jwtService.parse("invalid")).thenThrow(new RuntimeException("invalid"));

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    public void doFilterInternal_no_token() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
