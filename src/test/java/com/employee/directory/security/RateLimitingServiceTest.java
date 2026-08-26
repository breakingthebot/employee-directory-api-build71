/*
 * security/RateLimitingServiceTest.java
 * Unit tests for RateLimitingService verifying Bucket4j token bucket consumption.
 * Connects to: security/RateLimitingService.java
 * Created: 2026-08-08
 */
package com.employee.directory.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitingServiceTest {

    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        rateLimitingService = new RateLimitingService();
    }

    @Test
    @DisplayName("tryConsume - Allows requests when tokens are available")
    void tryConsume_AllowsWhenTokensAvailable() {
        boolean consumed = rateLimitingService.tryConsume("192.168.1.10");

        assertTrue(consumed);
        assertEquals(49, rateLimitingService.getAvailableTokens("192.168.1.10"));
    }

    @Test
    @DisplayName("tryConsume - Exhausts tokens after max capacity")
    void tryConsume_ExhaustsTokens() {
        String testIp = "10.0.0.1";

        for (int i = 0; i < 50; i++) {
            assertTrue(rateLimitingService.tryConsume(testIp));
        }

        assertFalse(rateLimitingService.tryConsume(testIp));
        assertEquals(0, rateLimitingService.getAvailableTokens(testIp));
    }
}
