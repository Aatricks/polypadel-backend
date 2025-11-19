package com.polypadel.security.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestAccessDeniedHandlerTest {

    @Test
    public void handle_serializes_json_body() throws Exception {
        RestAccessDeniedHandler handler = new RestAccessDeniedHandler();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse rsp = mock(HttpServletResponse.class);
        when(req.getRequestURI()).thenReturn("/some/path");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(rsp.getOutputStream()).thenReturn(new jakarta.servlet.ServletOutputStream() {
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(jakarta.servlet.WriteListener writeListener) { }
            @Override public void write(int b) { out.write(b); }
        });

        handler.handle(req, rsp, new org.springframework.security.access.AccessDeniedException("denied"));
        String json = out.toString();
        ObjectMapper mapper = new ObjectMapper();
        Map<?, ?> obj = mapper.readValue(json, Map.class);
        assertThat(obj.get("code")).isEqualTo("AUTH_FORBIDDEN");
        assertThat(obj.get("path")).isEqualTo("/some/path");
    }
}
