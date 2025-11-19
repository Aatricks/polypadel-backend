package com.polypadel.events;

import com.polypadel.common.exception.BusinessException;
import com.polypadel.events.dto.EventCreateRequest;
import com.polypadel.events.repository.EventRepository;
import com.polypadel.events.service.EventService;
import com.polypadel.events.mapper.EventMapper;
import com.polypadel.matches.repository.MatchRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class EventServiceTest {

    @Test
    void create_with_invalid_dates_throws() {
        var eventRepo = Mockito.mock(EventRepository.class);
        var matchRepo = Mockito.mock(MatchRepository.class);
        EventService svc = new EventService(eventRepo, matchRepo, Mockito.mock(EventMapper.class));

        EventCreateRequest req = new EventCreateRequest(LocalDate.now(), LocalDate.now().minusDays(1));

        assertThrows(BusinessException.class, () -> svc.create(req));
    }
}
