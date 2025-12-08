package com.polypadel.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record EventRequest(
    @NotNull @FutureOrPresent LocalDate eventDate,
    @NotNull LocalTime eventTime,
    @NotEmpty @Size(min = 1, max = 3) List<MatchInfo> matches
) {
    public record MatchInfo(
        @Min(1) @Max(10) int courtNumber,
        @NotNull Long team1Id,
        @NotNull Long team2Id
    ) {}
}
