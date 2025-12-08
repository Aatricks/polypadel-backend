package com.polypadel;

import com.polypadel.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EventServiceTest {

    @Autowired
    private EventService eventService;

    @Test
    void findAllEvents() {
        var events = eventService.findAll(null, null, null);
        assertNotNull(events);
    }

    @Test
    void findEventsWithMonth() {
        var events = eventService.findAll(null, null, "2025-12");
        assertNotNull(events);
    }

    @Test
    void eventNotFound() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.findById(99999L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
