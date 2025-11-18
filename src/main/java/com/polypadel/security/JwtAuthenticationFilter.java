package com.polypadel.security;

import com.polypadel.repository.JSONTokenRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final JSONTokenRepository jsonTokenRepository;

    public JwtAuthenticationFilter(JwtService jwtService, JSONTokenRepository jsonTokenRepository) {
        this.jwtService = jwtService;
        this.jsonTokenRepository = jsonTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = resolveToken(request);
            if (StringUtils.hasText(token)) {
                Claims claims = jwtService.parse(token);
                String jti = claims.getId();
                if (jti != null && jsonTokenRepository.existsByJtiAndRevokedTrue(jti)) {
                    filterChain.doFilter(request, response);
                    return;
                }
                String sub = claims.getSubject();
                String role = claims.get("role", String.class);
                var auth = new UsernamePasswordAuthenticationToken(sub, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            // Let the entry point handle 401 later in the chain
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // Cookie
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("JWT".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        // Authorization header
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
