package com.polypadel.auth.dto;

public record LoginResponse(String token, UserSummary user) {}
