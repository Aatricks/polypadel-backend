package com.polypadel.dto;

import java.time.LocalDate;
import java.util.List;

public record MyResultResponse(
    Long matchId,
    LocalDate date,
    Opponents opponents,
    String score,
    String result,
    Integer courtNumber
) {
    public record Opponents(String company, List<String> players) {}
}
