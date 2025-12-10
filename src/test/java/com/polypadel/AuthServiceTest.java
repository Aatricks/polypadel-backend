package com.polypadel;

import com.polypadel.dto.LoginRequest;
import com.polypadel.dto.LoginResponse;
import com.polypadel.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    void loginSuccess() {
        LoginResponse response = authService.login(new LoginRequest("admin@padel.com", "Admin@2025!"));
        assertNotNull(response.accessToken());
        assertEquals("bearer", response.tokenType());
        assertEquals("ADMINISTRATEUR", response.user().role());
    }

    @Test
    void loginInvalidPassword() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> authService.login(new LoginRequest("admin@padel.com", "wrong")));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertTrue(ex.getReason().contains("tentative"));
    }

    @Test
    void loginInvalidEmail() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> authService.login(new LoginRequest("nonexistent@test.com", "password")));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }
}
