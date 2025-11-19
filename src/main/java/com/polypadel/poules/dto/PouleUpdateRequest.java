package com.polypadel.poules.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PouleUpdateRequest(
    @NotBlank
    @Size(max = 64)
    String nom
) {}
