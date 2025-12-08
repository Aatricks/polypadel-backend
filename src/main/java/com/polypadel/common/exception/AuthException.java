package com.polypadel.common.exception;

public class AuthException extends BusinessException {
    private final Integer attemptsRemaining;
    private final Long minutesRemaining;

    public AuthException(String code, String message, Integer attemptsRemaining, Long minutesRemaining) {
        super(code, message);
        this.attemptsRemaining = attemptsRemaining;
        this.minutesRemaining = minutesRemaining;
    }

    public Integer getAttemptsRemaining() {
        return attemptsRemaining;
    }

    public Long getMinutesRemaining() {
        return minutesRemaining;
    }
}
