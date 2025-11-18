package com.polypadel.security;

import com.polypadel.domain.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private final Key key;
    private final int expHours;

    public JwtService(@Value("${security.jwt.secret}") String secret,
                      @Value("${security.jwt.expHours:24}") int expHours) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expHours = expHours;
    }

        public String generateToken(UUID userId, String email, Role role) {
        Instant now = Instant.now();
        Instant exp = now.plus(expHours, ChronoUnit.HOURS);
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .setSubject(userId.toString())
            .setId(jti)
                .addClaims(Map.of(
                        "email", email,
                        "role", role.name()
                ))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Instant getExpiration(String token) {
        return parse(token).getExpiration().toInstant();
    }

    public String getJti(String token) {
        return parse(token).getId();
    }
}
