package com.polypadel.events.dto;

import java.time.LocalDate;
import java.util.UUID;

public record EventResponse(
    UUID id,
    LocalDate dateDebut,
    LocalDate dateFin
) {}
