package com.polypadel.auth.service;

import com.polypadel.auth.repository.JSONTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenCleanupServiceTest {

    @Mock
    private JSONTokenRepository repo;

    private TokenCleanupService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new TokenCleanupService(repo, true, 7);
    }

    @Test
    void cleanupCallsRepositoryDeletes() {
        when(repo.deleteByExpirationBefore(any())).thenReturn(2L);
        when(repo.deleteByRevokedTrueAndExpirationBefore(any())).thenReturn(1L);
        service.cleanupExpiredTokens();
        ArgumentCaptor<Instant> cap1 = ArgumentCaptor.forClass(Instant.class);
        verify(repo, times(1)).deleteByExpirationBefore(cap1.capture());
        assertTrue(cap1.getValue().isBefore(Instant.now().plusSeconds(1)));
        ArgumentCaptor<Instant> cap2 = ArgumentCaptor.forClass(Instant.class);
        verify(repo, times(1)).deleteByRevokedTrueAndExpirationBefore(cap2.capture());
        assertTrue(cap2.getValue().isBefore(Instant.now().plusSeconds(1)));
    }
}
