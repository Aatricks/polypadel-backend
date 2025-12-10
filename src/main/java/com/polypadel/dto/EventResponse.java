package com.polypadel.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record EventResponse(
    Long id,
    LocalDate eventDate,
    LocalTime eventTime,
    List<MatchResponse> matches
) {}
