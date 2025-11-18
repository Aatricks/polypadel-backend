package com.polypadel.security;

import com.polypadel.domain.entity.JSONToken;
import com.polypadel.domain.entity.Utilisateur;
import com.polypadel.repository.JSONTokenRepository;
import com.polypadel.repository.UtilisateurRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class JWTLogoutHelper {

    private final JwtService jwtService;
    private final JSONTokenRepository jsonTokenRepository;
    private final UtilisateurRepository utilisateurRepository;

    public JWTLogoutHelper(JwtService jwtService, JSONTokenRepository jsonTokenRepository, UtilisateurRepository utilisateurRepository) {
        this.jwtService = jwtService;
        this.jsonTokenRepository = jsonTokenRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            String token = resolveToken(request);
            if (StringUtils.hasText(token)) {
                Claims claims = jwtService.parse(token);
                String jti = claims.getId();
                String sub = claims.getSubject();
                Instant exp = claims.getExpiration().toInstant();
                if (StringUtils.hasText(jti) && StringUtils.hasText(sub)) {
                    Optional<Utilisateur> userOpt = utilisateurRepository.findById(UUID.fromString(sub));
                    userOpt.ifPresent(u -> {
                        JSONToken jt = new JSONToken();
                        jt.setUtilisateur(u);
                        jt.setJti(jti);
                        jt.setExpiration(exp);
                        jt.setRevoked(true);
                        jsonTokenRepository.save(jt);
                    });
                }
            }
        } catch (Exception ignored) {
            // ignore parse errors; still clear cookie
        }
        // Clear cookie
        ResponseCookie clear = ResponseCookie.from("JWT", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", clear.toString());
    }

    private String resolveToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("JWT".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        String header = request.getHeader(org.springframework.http.HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
