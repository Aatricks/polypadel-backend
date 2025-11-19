package com.polypadel.auth.web;

import com.polypadel.auth.dto.LoginRequest;
import com.polypadel.auth.dto.LoginResponse;
import com.polypadel.auth.dto.UserSummary;
import com.polypadel.auth.service.AuthService;
import com.polypadel.security.JWTLogoutHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class AuthControllerTest {

    private AuthService authService;
    private JWTLogoutHelper logoutHelper;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        authService = Mockito.mock(AuthService.class);
        logoutHelper = Mockito.mock(JWTLogoutHelper.class);
        controller = new AuthController(authService, Optional.of(logoutHelper));
    }

    @Test
    void login_sets_cookie_and_returns_response() {
        LoginRequest req = new LoginRequest("  TEST@Ex.com  ", "password");
        UUID id = UUID.randomUUID();
        LoginResponse resp = new LoginResponse("token", new UserSummary(id, "test@ex.com", "ADMIN"));
        Mockito.when(authService.login(eq("test@ex.com"), eq("password"))).thenReturn(resp);
        HttpServletResponse httpResp = Mockito.mock(HttpServletResponse.class);

        ResponseEntity<LoginResponse> r = controller.login(req, httpResp);
        assertEquals(200, r.getStatusCodeValue());
        Mockito.verify(httpResp).addHeader(eq("Set-Cookie"), any(String.class));
        assertEquals(resp, r.getBody());
    }

    @Test
    void logout_calls_helper_and_returns_no_content() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse httpResp = Mockito.mock(HttpServletResponse.class);
        var r = controller.logout(req, httpResp);
        assertEquals(204, r.getStatusCodeValue());
        Mockito.verify(logoutHelper).logout(eq(req), eq(httpResp));
    }
}
