package com.polypadel.events.mapper;

import com.polypadel.domain.entity.Evenement;
import com.polypadel.events.dto.EventResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class EventMapperTest {
    private final EventMapper mapper = Mappers.getMapper(EventMapper.class);

    @Test
    public void toResponse_maps_dates() {
        Evenement e = new Evenement();
        UUID id = UUID.randomUUID(); e.setId(id);
        e.setDateDebut(LocalDate.of(2020, 1, 1));
        e.setDateFin(LocalDate.of(2020, 1, 2));
        EventResponse r = mapper.toResponse(e);
        assertThat(r.id()).isEqualTo(id);
        assertThat(r.dateDebut()).isEqualTo(e.getDateDebut());
        assertThat(r.dateFin()).isEqualTo(e.getDateFin());
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
