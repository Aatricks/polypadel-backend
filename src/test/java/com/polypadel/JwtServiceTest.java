package com.polypadel;

import com.polypadel.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void generateAndValidateToken() {
        String token = jwtService.generateToken(1L, "test@test.com", "JOUEUR");
        assertNotNull(token);
        assertTrue(jwtService.isValid(token));
    }

    @Test
    void parseToken() {
        String token = jwtService.generateToken(42L, "user@test.com", "ADMINISTRATEUR");
        var claims = jwtService.parseToken(token);
        assertEquals("42", claims.getSubject());
        assertEquals("user@test.com", claims.get("email"));
        assertEquals("ADMINISTRATEUR", claims.get("role"));
    }

    @Test
    void invalidToken() {
        assertFalse(jwtService.isValid("invalid.token.here"));
    }
}
