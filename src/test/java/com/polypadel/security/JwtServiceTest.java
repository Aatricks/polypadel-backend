package com.polypadel.security;

import com.polypadel.domain.enums.Role;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtServiceTest {

    @Test
    public void generate_and_parse_token_ok() {
        String secret = Base64.getEncoder().encodeToString("01234567890123456789012345678901".getBytes());
        JwtService svc = new JwtService(secret, 24);
        UUID id = UUID.randomUUID();
        String token = svc.generateToken(id, "a@b.com", Role.JOUEUR);
        Claims claims = svc.parse(token);
        assertThat(claims.getSubject()).isEqualTo(id.toString());
        assertThat(claims.get("email", String.class)).isEqualTo("a@b.com");
        assertThat(claims.get("role", String.class)).isEqualTo(Role.JOUEUR.name());
        Instant exp = svc.getExpiration(token);
        assertThat(exp).isAfter(Instant.now());
        assertThat(svc.getJti(token)).isNotNull();
    }
}
