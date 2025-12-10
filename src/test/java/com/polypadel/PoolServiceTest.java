package com.polypadel;

import com.polypadel.dto.PoolRequest;
import com.polypadel.service.PoolService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PoolServiceTest {

    @Autowired
    private PoolService poolService;

    @Test
    void findAllPools() {
        var pools = poolService.findAll();
        assertNotNull(pools);
    }

    @Test
    void createPoolNotSixTeams() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> poolService.create(new PoolRequest("Test Pool", List.of(1L, 2L, 3L))));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("6"));
    }

    @Test
    void poolNotFound() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> poolService.findById(99999L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
