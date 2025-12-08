package com.polypadel.dto;

public record ProfileResponse(
    UserInfo user,
    PlayerResponse player
) {
    public record UserInfo(Long id, String email, String role) {}
}
