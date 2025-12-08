package com.polypadel.dto;

public record LoginResponse(
    String accessToken,
    String tokenType,
    UserDto user
) {
    public record UserDto(Long id, String email, String role, boolean mustChangePassword) {}
}
