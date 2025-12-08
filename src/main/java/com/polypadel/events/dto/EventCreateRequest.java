package com.polypadel.events.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record EventCreateRequest(
    @NotNull 
    LocalDate eventDate,

    @NotNull 
    LocalTime eventTime, 

    @NotEmpty(message = "Un événement doit contenir au moins un match") 
    List<MatchSubRequest> matches 
) {
    
    public record MatchSubRequest(
        @NotNull @Min(1) @Max(10) Integer courtNumber, 
        @NotNull UUID team1Id,                         
        @NotNull UUID team2Id                          
    ) {}
}