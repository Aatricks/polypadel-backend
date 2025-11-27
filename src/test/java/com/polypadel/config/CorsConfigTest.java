package com.polypadel.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

public class CorsConfigTest {

    @Test
    void star_allowed_origin_adds_pattern() {
        CorsConfig cfg = new CorsConfig();
        CorsConfigurationSource src = cfg.corsConfigurationSource("*");
        assertTrue(src instanceof UrlBasedCorsConfigurationSource);
        UrlBasedCorsConfigurationSource usrc = (UrlBasedCorsConfigurationSource) src;
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/");
        CorsConfiguration c = usrc.getCorsConfiguration(req);
        assertNotNull(c);
        assertTrue(c.getAllowedOriginPatterns().contains("*"));
        assertTrue(c.getAllowedHeaders().contains("*"));
        assertTrue(c.getAllowedMethods().contains("GET"));
        assertTrue(c.getAllowedMethods().contains("POST"));
        assertTrue(c.getAllowedMethods().contains("PUT"));
        assertTrue(c.getAllowedMethods().contains("DELETE"));
        assertTrue(c.getAllowedMethods().contains("OPTIONS"));
        assertTrue(c.getAllowCredentials());
    }

    @Test
    void explicit_allowed_origins_are_parsed() {
        CorsConfig cfg = new CorsConfig();
        CorsConfigurationSource src = cfg.corsConfigurationSource("https://a.example.com, https://b.example.com");
        UrlBasedCorsConfigurationSource usrc = (UrlBasedCorsConfigurationSource) src;
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/");
        CorsConfiguration c = usrc.getCorsConfiguration(req);
        assertNotNull(c);
        assertTrue(c.getAllowedOrigins().contains("https://a.example.com"));
        assertTrue(c.getAllowedOrigins().contains("https://b.example.com"));
        assertNull(c.getAllowedOriginPatterns());
    }
}
