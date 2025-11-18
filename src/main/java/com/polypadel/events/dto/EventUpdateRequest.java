package com.polypadel.events.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class EventUpdateRequest {
    @NotNull
    public LocalDate dateDebut;
    @NotNull
    public LocalDate dateFin;
}
