package com.polypadel.events.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class EventCreateRequest {
    @NotNull
    public LocalDate dateDebut;
    @NotNull
    public LocalDate dateFin;
}
