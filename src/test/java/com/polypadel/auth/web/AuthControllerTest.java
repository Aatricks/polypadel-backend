package com.polypadel.auth.web;

import com.polypadel.auth.dto.LoginRequest;
import com.polypadel.auth.dto.LoginResponse;
import com.polypadel.auth.dto.UserSummary;
import com.polypadel.auth.service.AuthService;
import com.polypadel.security.JWTLogoutHelper;
import com.polypadel.security.JwtService;
import java.time.Instant;
import java.time.Duration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
// no static includes here
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class AuthControllerTest {

    private AuthService authService;
    private JWTLogoutHelper logoutHelper;
    private JwtService jwtService;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        authService = Mockito.mock(AuthService.class);
        logoutHelper = Mockito.mock(JWTLogoutHelper.class);
        jwtService = Mockito.mock(JwtService.class);
        controller = new AuthController(authService, Optional.of(logoutHelper), jwtService, false);
    }

    @Test
    void login_returns_response() {
        LoginRequest req = new LoginRequest("  TEST@Ex.com  ", "password");
        UUID id = UUID.randomUUID();
        LoginResponse resp = new LoginResponse("token", new UserSummary(id, "test@ex.com", "ADMIN"));
        Mockito.when(authService.login(eq("test@ex.com"), eq("password"))).thenReturn(resp);
        HttpServletResponse httpResp = Mockito.mock(HttpServletResponse.class);
        Mockito.when(jwtService.getExpiration("token")).thenReturn(Instant.now().plus(Duration.ofHours(1)));

        ResponseEntity<LoginResponse> r = controller.login(req, httpResp);
        assertEquals(200, r.getStatusCode().value());
        assertEquals(resp, r.getBody());
        Mockito.verify(httpResp).addHeader(eq("Set-Cookie"), Mockito.contains("JWT="));
    }

    @Test
    void logout_calls_helper_and_returns_no_content() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse httpResp = Mockito.mock(HttpServletResponse.class);
        var r = controller.logout(req, httpResp);
        assertEquals(204, r.getStatusCode().value());
        Mockito.verify(logoutHelper).logout(eq(req), eq(httpResp));
    }

    @Test
    void login_locked_returns_locked_status() throws Exception {
        // Setup controller with mocked service to throw BusinessException with AUTH_ACCOUNT_LOCKED
        AuthService authServiceMock = Mockito.mock(AuthService.class);
        Mockito.when(authServiceMock.login(eq("user@example.com"), eq("Wrong")))
            .thenThrow(new com.polypadel.common.exception.BusinessException("AUTH_ACCOUNT_LOCKED", "Account locked"));
        Mockito.when(jwtService.getExpiration(Mockito.anyString())).thenReturn(Instant.now().plus(Duration.ofHours(1)));
        AuthController controllerWithMock = new AuthController(authServiceMock, Optional.empty(), jwtService, false);

        // Use MockMvc standalone setup to test mapping via GlobalExceptionHandler
        org.springframework.test.web.servlet.MockMvc mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
            .standaloneSetup(controllerWithMock)
            .setControllerAdvice(new com.polypadel.common.exception.GlobalExceptionHandler())
            .build();

        String body = "{\"email\":\"user@example.com\",\"password\":\"Wrong\"}";
        mockMvc.perform(post("/auth/login")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isLocked())
            .andExpect(jsonPath("$.code").value("AUTH_ACCOUNT_LOCKED"));
        }
}
