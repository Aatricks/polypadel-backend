package com.polypadel;

import static org.junit.jupiter.api.Assertions.*;

import com.polypadel.dto.LoginRequest;
import com.polypadel.dto.LoginResponse;
import com.polypadel.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    @Transactional
    void loginSuccess() {
        LoginResponse response = authService.login(
            new LoginRequest("admin@padel.com", "Admin@2025!")
        );
        assertNotNull(response.accessToken());
        assertEquals("bearer", response.tokenType());
        assertEquals("ADMINISTRATEUR", response.user().role());
    }

    @Test
    @Transactional
    void loginInvalidPassword() {
        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () ->
                authService.login(new LoginRequest("admin@padel.com", "wrong"))
        );
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertTrue(ex.getReason().contains("tentative"));
    }

    @Test
    @Transactional
    void loginInvalidEmail() {
        String uniqueEmail =
            "invalid" + System.currentTimeMillis() + "@test.com";
        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> authService.login(new LoginRequest(uniqueEmail, "password"))
        );
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }
}
