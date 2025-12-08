package com.polypadel.security;

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

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String token = resolveToken(request);
            if (StringUtils.hasText(token)) {
                Claims claims = jwtService.parse(token);
                String sub = claims.getSubject();
                String role = claims.get("role", String.class);
                var auth = new UsernamePasswordAuthenticationToken(sub, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception ignored) {}
        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("JWT".equals(c.getName())) return c.getValue();
            }
        }
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) return header.substring(7);
        return null;
    }
}
