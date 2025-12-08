package com.polypadel.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidation_builds_error_response() {
        MethodArgumentNotValidException ex = Mockito.mock(MethodArgumentNotValidException.class);
        BindingResult br = Mockito.mock(BindingResult.class);
        Mockito.when(ex.getBindingResult()).thenReturn(br);
        Mockito.when(br.getFieldErrors()).thenReturn(List.of(new FieldError("obj", "field", "err")));

        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getRequestURI()).thenReturn("/test");

        var resp = handler.handleValidation(ex, req);
        assertEquals(400, resp.getStatusCodeValue());
        var body = resp.getBody();
        assertNotNull(body);
        assertEquals("VALIDATION_FAILED", body.code);
        assertTrue(body.message.contains("field: err"));
    }

    @Test
    void handleBusiness_returns_conflict() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getRequestURI()).thenReturn("/test");
        BusinessException ex = new BusinessException("CODE", "msg");

        var resp = handler.handleBusiness(ex, req);
        assertEquals(409, resp.getStatusCodeValue());
        assertEquals("CODE", resp.getBody().code);
    }

    @Test
    void handleBusiness_locked_returns_423() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getRequestURI()).thenReturn("/test");
        BusinessException ex = new BusinessException("AUTH_ACCOUNT_LOCKED", "locked");

        var resp = handler.handleBusiness(ex, req);
        assertEquals(423, resp.getStatusCodeValue());
    }

    @Test
    void handleOther_returns_500() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getRequestURI()).thenReturn("/test");

        var resp = handler.handleOther(new Exception("boom"), req);
        assertEquals(500, resp.getStatusCodeValue());
        assertEquals("UNEXPECTED_ERROR", resp.getBody().code);
    }

    @Test
    void handleNotFound_returns_404() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getRequestURI()).thenReturn("/not/found");
        NotFoundException ex = new NotFoundException("PLAYER_NOT_FOUND", "missing");

        var resp = handler.handleNotFound(ex, req);
        assertEquals(404, resp.getStatusCodeValue());
    }
}
