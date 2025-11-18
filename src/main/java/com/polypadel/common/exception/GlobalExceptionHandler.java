package com.polypadel.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

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
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse();
        body.path = req.getRequestURI();
        body.status = HttpStatus.CONFLICT.value();
        body.error = HttpStatus.CONFLICT.getReasonPhrase();
        body.code = ex.getCode();
        body.message = ex.getMessage();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse();
        body.path = req.getRequestURI();
        body.status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        body.error = HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
        body.code = "UNEXPECTED_ERROR";
        body.message = ex.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private String formatFieldError(FieldError fe) {
        return fe.getField() + ": " + fe.getDefaultMessage();
    }
}
