package com.polypadel.security.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse resp, AuthenticationException ex) throws IOException {
        resp.setStatus(401);
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(resp.getOutputStream(), Map.of("status", 401, "code", "UNAUTHORIZED", "message", ex.getMessage()));
    }
}
