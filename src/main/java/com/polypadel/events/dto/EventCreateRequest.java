package com.polypadel.events.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record EventCreateRequest(
    @NotNull LocalDate dateDebut,
    @NotNull LocalDate dateFin
) {}
