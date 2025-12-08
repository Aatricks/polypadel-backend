package com.polypadel.events.mapper;

import com.polypadel.domain.entity.Evenement;
import com.polypadel.events.dto.EventResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalTime; 
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class EventMapperImplTest {
    
    
    private final EventMapper mapper = Mappers.getMapper(EventMapper.class);

    @Test
    public void toResponse_and_list() {
     
        Evenement e = new Evenement(); 
        e.setId(UUID.randomUUID());
        
    
        e.setEventDate(LocalDate.now());     
        e.setEventTime(LocalTime.of(10, 0));  
        e.setMatches(new ArrayList<>());    
        
        EventResponse resp = mapper.toResponse(e);

       
        assertThat(resp).isNotNull();
        assertThat(resp.eventDate()).isEqualTo(e.getEventDate()); 
        assertThat(resp.eventTime()).isEqualTo(e.getEventTime());

        var list = mapper.toResponseList(List.of(e));
        assertThat(list).hasSize(1);
    }
}