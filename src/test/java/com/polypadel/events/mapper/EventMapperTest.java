package com.polypadel.events.mapper;

import com.polypadel.domain.entity.Evenement;
import com.polypadel.events.dto.EventResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class EventMapperTest {
    
   
    private final EventMapper mapper = Mappers.getMapper(EventMapper.class);

    @Test
    public void toResponse_maps_dates() {
        // Given
        Evenement e = new Evenement();
        UUID id = UUID.randomUUID(); 
        e.setId(id);
        
        // --- MISE À JOUR DES CHAMPS ---
        e.setEventDate(LocalDate.of(2020, 1, 1)); // Remplace dateDebut
        e.setEventTime(LocalTime.of(14, 30));     // Remplace dateFin
        e.setMatches(new ArrayList<>());          // Initialiser la liste pour éviter le null
        
        EventResponse r = mapper.toResponse(e);

   
        assertThat(r.id()).isEqualTo(id);
        
      
        assertThat(r.eventDate()).isEqualTo(e.getEventDate());
        assertThat(r.eventTime()).isEqualTo(e.getEventTime());
    }

    @Test
    public void toResponse_returns_null_for_null_entity() {
        EventResponse r = mapper.toResponse(null);
        assertThat(r).isNull();
    }

    @Test
    public void toResponseList_handles_null_input() {
        assertThat(mapper.toResponseList(null)).isNull();
    }
}