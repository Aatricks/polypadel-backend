package com.polypadel.auth.service;

import com.polypadel.auth.repository.JSONTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenCleanupService {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupService.class);

    private final JSONTokenRepository jsonTokenRepository;

    private final boolean enabled;
    private final int revokedRetentionDays;

    public TokenCleanupService(JSONTokenRepository jsonTokenRepository,
                               @Value("${security.token.cleanup.enabled:true}") boolean enabled,
                               @Value("${security.token.revoked-retention-days:7}") int revokedRetentionDays) {
        this.jsonTokenRepository = jsonTokenRepository;
        this.enabled = enabled;
        this.revokedRetentionDays = revokedRetentionDays;
    }

    /**
     * Clean up expired tokens and revoked tokens older than configured retention.
     * Runs daily at 01:00.
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void cleanupExpiredTokensScheduled() {
        if (!enabled) {
            log.debug("Token cleanup disabled by configuration");
            return;
        }
        cleanupExpiredTokens();
    }

    // public for unit testing
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        long expired = jsonTokenRepository.deleteByExpirationBefore(now);
        log.info("Deleted {} expired tokens", expired);

        Instant retentionBoundary = now.minusSeconds((long) revokedRetentionDays * 24 * 3600);
        long revokedCleaned = jsonTokenRepository.deleteByRevokedTrueAndExpirationBefore(retentionBoundary);
        log.info("Deleted {} revoked tokens older than {} days", revokedCleaned, revokedRetentionDays);
    }
}
