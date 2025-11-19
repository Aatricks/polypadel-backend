package com.polypadel.admin.dto;

import java.util.UUID;

public record AdminCreateUserResponse(
    UUID id,
    String email,
    String role,
    String tempPassword
) {}
