package com.polypadel.events.dto;

import com.polypadel.matches.dto.MatchResponse; 
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record EventResponse(
    UUID id,
    
  
    LocalDate eventDate, 
    
    
    LocalTime eventTime,
    
   
    List<MatchResponse> matches 
) {}