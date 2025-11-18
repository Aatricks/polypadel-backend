package com.polypadel.poules.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PouleCreateRequest {
    @NotBlank
    @Size(max = 64)
    public String nom;
}
