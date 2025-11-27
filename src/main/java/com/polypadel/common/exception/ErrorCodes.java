package com.polypadel.common.exception;

public final class ErrorCodes {
    private ErrorCodes() {}

    public static final String AUTH_INVALID_CREDENTIALS = "AUTH_INVALID_CREDENTIALS";
    public static final String AUTH_INACTIVE_ACCOUNT = "AUTH_INACTIVE_ACCOUNT";
    public static final String AUTH_MISSING_ROLE = "AUTH_MISSING_ROLE";
    public static final String AUTH_PASSWORDS_DO_NOT_MATCH = "AUTH_PASSWORDS_DO_NOT_MATCH";
    public static final String AUTH_EMAIL_ALREADY_EXISTS = "AUTH_EMAIL_ALREADY_EXISTS";
}
