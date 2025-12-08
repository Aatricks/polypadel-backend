package com.polypadel.security.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, AccessDeniedException ex) throws IOException {
        resp.setStatus(403);
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(resp.getOutputStream(), Map.of("status", 403, "code", "FORBIDDEN", "message", ex.getMessage()));
    }
}
