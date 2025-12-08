package com.polypadel.dto;

import java.time.LocalDate;

public record PlayerResponse(
    Long id,
    String firstName,
    String lastName,
    String company,
    String licenseNumber,
    LocalDate birthDate,
    String photoUrl,
    boolean hasAccount
) {}
