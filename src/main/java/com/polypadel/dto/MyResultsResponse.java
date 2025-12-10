package com.polypadel.dto;

import java.util.List;

public record MyResultsResponse(
    List<MyResultResponse> results,
    Statistics statistics
) {
    public record Statistics(int totalMatches, int wins, int losses, double winRate) {}
}
