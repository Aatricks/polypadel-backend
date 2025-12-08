package com.polypadel.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage()).toList();
        return ResponseEntity.badRequest().body(new ErrorResponse(req.getRequestURI(), 400, "VALIDATION_FAILED", String.join(", ", errors)));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest req) {
        HttpStatus status = switch (ex.getCode()) {
            case "AUTH_ACCOUNT_LOCKED" -> HttpStatus.LOCKED;
            case "AUTH_INVALID_CREDENTIALS" -> HttpStatus.UNAUTHORIZED;
            case "AUTH_INACTIVE_ACCOUNT", "AUTH_MISSING_ROLE" -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.CONFLICT;
        };
        ErrorResponse body = new ErrorResponse(req.getRequestURI(), status.value(), ex.getCode(), ex.getMessage());
        if (ex instanceof AuthException ae) {
            body.attemptsRemaining = ae.getAttemptsRemaining();
            body.minutesRemaining = ae.getMinutesRemaining();
        }
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(req.getRequestURI(), 404, ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(req.getRequestURI(), 500, "UNEXPECTED_ERROR", ex.getMessage()));
    }
}
