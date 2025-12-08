package com.polypadel.dto;

import java.util.List;

public record PoolResponse(
    Long id,
    String name,
    int teamsCount,
    List<TeamResponse> teams
) {}
