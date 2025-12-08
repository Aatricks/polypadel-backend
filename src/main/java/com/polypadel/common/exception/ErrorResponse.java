package com.polypadel.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    public String path;
    public int status;
    public String code;
    public String message;
    public Integer attemptsRemaining;
    public Long minutesRemaining;

    public ErrorResponse() {}

    public ErrorResponse(String path, int status, String code, String message) {
        this.path = path;
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
