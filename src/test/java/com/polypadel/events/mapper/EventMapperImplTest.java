package com.polypadel.events.mapper;

import com.polypadel.domain.entity.Evenement;
import com.polypadel.events.dto.EventResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class EventMapperImplTest {
    private final EventMapper mapper = Mappers.getMapper(EventMapper.class);

    @Test
    public void toResponse_and_list() {
        Evenement e = new Evenement(); e.setId(UUID.randomUUID()); e.setDateDebut(LocalDate.now()); e.setDateFin(LocalDate.now());
        EventResponse resp = mapper.toResponse(e);
        assertThat(resp).isNotNull();
        var list = mapper.toResponseList(List.of(e));
        assertThat(list).hasSize(1);
    }
}
