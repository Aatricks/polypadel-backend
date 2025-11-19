package com.polypadel.poules.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PouleCreateRequest(
    @NotBlank
    @Size(max = 64)
    String nom
) {}
