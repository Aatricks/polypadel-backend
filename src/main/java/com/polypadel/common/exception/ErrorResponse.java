package com.polypadel.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

public class ErrorResponse {
    public String timestamp = Instant.now().toString();
    public String path;
    public int status;
    public String error;
    public String code;
    public String message;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<String> details;
    public String traceId;
    @com.fasterxml.jackson.annotation.JsonProperty("attempts_remaining")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer attemptsRemaining;
    @com.fasterxml.jackson.annotation.JsonProperty("minutes_remaining")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Long minutesRemaining;
}
