package com.polypadel.dto;

import java.util.List;

public record TeamResponse(
    Long id,
    String company,
    List<PlayerInfo> players,
    PoolInfo pool
) {
    public record PlayerInfo(Long id, String firstName, String lastName) {}
    public record PoolInfo(Long id, String name) {}
}
