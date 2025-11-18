package com.polypadel.events;

import com.polypadel.common.exception.BusinessException;
import com.polypadel.events.dto.EventCreateRequest;
import com.polypadel.events.repository.EventRepository;
import com.polypadel.events.service.EventService;
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
        EventService svc = new EventService(eventRepo, matchRepo);

        EventCreateRequest req = new EventCreateRequest();
        req.dateDebut = LocalDate.now();
        req.dateFin = req.dateDebut.minusDays(1);

        assertThrows(BusinessException.class, () -> svc.create(req));
    }
}
