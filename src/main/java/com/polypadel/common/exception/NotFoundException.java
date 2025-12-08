package com.polypadel.common.exception;

/**
 * Indicates a requested resource was not found.
 */
public class NotFoundException extends BusinessException {

    public NotFoundException(String code, String message) {
        super(code, message);
    }
}
