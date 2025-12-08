package com.polypadel.common.exception;

public final class ErrorCodes {
    private ErrorCodes() {}

    public static final String AUTH_INVALID_CREDENTIALS = "AUTH_INVALID_CREDENTIALS";
    public static final String AUTH_INACTIVE_ACCOUNT = "AUTH_INACTIVE_ACCOUNT";
    public static final String AUTH_MISSING_ROLE = "AUTH_MISSING_ROLE";
    public static final String AUTH_PASSWORDS_DO_NOT_MATCH = "AUTH_PASSWORDS_DO_NOT_MATCH";
    public static final String AUTH_EMAIL_ALREADY_EXISTS = "AUTH_EMAIL_ALREADY_EXISTS";
    public static final String AUTH_ACCOUNT_LOCKED = "AUTH_ACCOUNT_LOCKED";

    // Generic not found codes
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String EVENT_NOT_FOUND = "EVENT_NOT_FOUND";
    public static final String TEAM_NOT_FOUND = "TEAM_NOT_FOUND";
    public static final String PLAYER_NOT_FOUND = "PLAYER_NOT_FOUND";
    public static final String POULE_NOT_FOUND = "POULE_NOT_FOUND";
    public static final String MATCH_NOT_FOUND = "MATCH_NOT_FOUND";
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
}
