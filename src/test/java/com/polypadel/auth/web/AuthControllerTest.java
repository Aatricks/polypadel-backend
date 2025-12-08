package com.polypadel.auth.web;

import com.polypadel.auth.dto.LoginRequest;
import com.polypadel.auth.dto.LoginResponse;
import com.polypadel.auth.dto.UserSummary;
import com.polypadel.auth.service.AuthService;
import com.polypadel.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class AuthControllerTest {

    private AuthService authService;
    private JwtService jwtService;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        authService = Mockito.mock(AuthService.class);
        jwtService = Mockito.mock(JwtService.class);
        controller = new AuthController(authService, jwtService);
    }

    @Test
    void login_returns_response() {
        LoginRequest req = new LoginRequest("  TEST@Ex.com  ", "password");
        UUID id = UUID.randomUUID();
        LoginResponse resp = new LoginResponse("token", new UserSummary(id, "test@ex.com", "ADMIN"));
        Mockito.when(authService.login(eq("test@ex.com"), eq("password"))).thenReturn(resp);
        Mockito.when(jwtService.getExpiration("token")).thenReturn(Instant.now().plus(Duration.ofHours(1)));
        HttpServletResponse httpResp = Mockito.mock(HttpServletResponse.class);

        ResponseEntity<LoginResponse> r = controller.login(req, httpResp);
        assertEquals(200, r.getStatusCode().value());
        assertEquals(resp, r.getBody());
        Mockito.verify(httpResp).addHeader(eq("Set-Cookie"), Mockito.contains("JWT="));
    }

    @Test
    void logout_clears_cookie() {
        HttpServletResponse httpResp = Mockito.mock(HttpServletResponse.class);
        var r = controller.logout(httpResp);
        assertEquals(204, r.getStatusCode().value());
        Mockito.verify(httpResp).addHeader(eq("Set-Cookie"), Mockito.contains("Max-Age=0"));
    }

    @Test
    void login_locked_returns_locked_status() throws Exception {
        AuthService authServiceMock = Mockito.mock(AuthService.class);
        Mockito.when(authServiceMock.login(eq("user@example.com"), eq("Wrong")))
            .thenThrow(new com.polypadel.common.exception.BusinessException("AUTH_ACCOUNT_LOCKED", "Account locked"));
        AuthController controllerMock = new AuthController(authServiceMock, jwtService);

        var mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
            .standaloneSetup(controllerMock)
            .setControllerAdvice(new com.polypadel.common.exception.GlobalExceptionHandler())
            .build();

        mockMvc.perform(post("/auth/login")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"email\":\"user@example.com\",\"password\":\"Wrong\"}"))
            .andExpect(status().isLocked())
            .andExpect(jsonPath("$.code").value("AUTH_ACCOUNT_LOCKED"));
    }
}
