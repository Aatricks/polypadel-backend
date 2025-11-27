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
    void handleValidation_builds_error_response_with_details() {
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
        assertTrue(body.details.contains("field: err"));
        assertEquals("/test", body.path);
    }

    @Test
    void handleBusiness_returns_conflict_with_business_code() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getRequestURI()).thenReturn("/test");
        BusinessException ex = new BusinessException("CODE", "msg");

        var resp = handler.handleBusiness(ex, req);
        assertEquals(409, resp.getStatusCodeValue());
        var body = resp.getBody();
        assertEquals("CODE", body.code);
        assertEquals("msg", body.message);
        assertEquals("/test", body.path);
    }

    @Test
    void handleOther_returns_500() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getRequestURI()).thenReturn("/test");
        Exception ex = new Exception("boom");

        var resp = handler.handleOther(ex, req);
        assertEquals(500, resp.getStatusCodeValue());
        var body = resp.getBody();
        assertEquals("UNEXPECTED_ERROR", body.code);
        assertEquals("boom", body.message);
        assertEquals("/test", body.path);
    }
}
