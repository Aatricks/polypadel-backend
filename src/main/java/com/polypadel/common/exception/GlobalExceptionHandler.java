package com.polypadel.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import com.polypadel.common.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse();
        body.path = req.getRequestURI();
        body.status = HttpStatus.BAD_REQUEST.value();
        body.error = HttpStatus.BAD_REQUEST.getReasonPhrase();
        body.code = "VALIDATION_FAILED";
        body.message = "Validation failed";
        body.details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError).toList();
        body.traceId = MDC.get("traceId");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse();
        body.path = req.getRequestURI();
        HttpStatus status = HttpStatus.CONFLICT;
        // Map some auth-related business codes to more appropriate status codes
        if (com.polypadel.common.exception.ErrorCodes.AUTH_ACCOUNT_LOCKED.equals(ex.getCode())) {
            status = HttpStatus.LOCKED;
        } else if (com.polypadel.common.exception.ErrorCodes.AUTH_INVALID_CREDENTIALS.equals(ex.getCode())) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (com.polypadel.common.exception.ErrorCodes.AUTH_INACTIVE_ACCOUNT.equals(ex.getCode()) ||
                com.polypadel.common.exception.ErrorCodes.AUTH_MISSING_ROLE.equals(ex.getCode())) {
            status = HttpStatus.FORBIDDEN;
        }
        body.status = status.value();
        body.error = status.getReasonPhrase();
        body.code = ex.getCode();
        // Map English backend messages to front-end friendly French messages for UX/testing
        String message = ex.getMessage();
        if (com.polypadel.common.exception.ErrorCodes.AUTH_INVALID_CREDENTIALS.equals(ex.getCode())) {
            message = "Email ou mot de passe incorrect";
        } else if (com.polypadel.common.exception.ErrorCodes.AUTH_ACCOUNT_LOCKED.equals(ex.getCode())) {
            message = "Compte bloqué";
        } else if (com.polypadel.common.exception.ErrorCodes.AUTH_INACTIVE_ACCOUNT.equals(ex.getCode())) {
            message = "Compte inactif";
        } else if (com.polypadel.common.exception.ErrorCodes.AUTH_MISSING_ROLE.equals(ex.getCode())) {
            message = "Rôle manquant";
        }
        body.message = message;
        body.traceId = MDC.get("traceId");

        // If this is an AuthException, include attempt/lockout metadata
        if (ex instanceof com.polypadel.common.exception.AuthException ae) {
            body.attemptsRemaining = ae.getAttemptsRemaining();
            body.minutesRemaining = ae.getMinutesRemaining();
        }
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse();
        body.path = req.getRequestURI();
        HttpStatus status = HttpStatus.NOT_FOUND;
        body.status = status.value();
        body.error = status.getReasonPhrase();
        body.code = ex.getCode();
        body.message = ex.getMessage();
        body.traceId = MDC.get("traceId");
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse();
        body.path = req.getRequestURI();
        body.status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        body.error = HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
        body.code = "UNEXPECTED_ERROR";
        body.message = ex.getMessage();
        body.traceId = MDC.get("traceId");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private String formatFieldError(FieldError fe) {
        return fe.getField() + ": " + fe.getDefaultMessage();
    }
}
