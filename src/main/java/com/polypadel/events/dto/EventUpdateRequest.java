package com.polypadel.events.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record EventUpdateRequest(
    
    LocalDate eventDate,
    LocalTime eventTime
) {}