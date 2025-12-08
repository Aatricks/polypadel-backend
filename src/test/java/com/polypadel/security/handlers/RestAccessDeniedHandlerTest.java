package com.polypadel.security.handlers;

import org.junit.jupiter.api.Test;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RestAccessDeniedHandlerTest {

    @Test
    public void handle_returns_403() throws Exception {
        var handler = new RestAccessDeniedHandler();
        var req = mock(HttpServletRequest.class);
        var rsp = mock(HttpServletResponse.class);
        var out = new ByteArrayOutputStream();
        when(rsp.getOutputStream()).thenReturn(new jakarta.servlet.ServletOutputStream() {
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(jakarta.servlet.WriteListener l) { }
            @Override public void write(int b) { out.write(b); }
        });

        handler.handle(req, rsp, new org.springframework.security.access.AccessDeniedException("denied"));
        verify(rsp).setStatus(403);
        assertThat(out.toString()).contains("FORBIDDEN");
    }
}
