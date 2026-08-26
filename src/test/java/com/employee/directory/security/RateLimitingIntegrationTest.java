/*
 * security/RateLimitingIntegrationTest.java
 * Integration tests for RateLimitingFilter verifying HTTP headers and 429 throttling response.
 * Connects to: security/RateLimitingFilter.java, security/RateLimitingService.java
 * Created: 2026-08-08
 */
package com.employee.directory.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RateLimitingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/employees - Sets X-Rate-Limit-Remaining header")
    void request_IncludesRateLimitHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/employees").header("X-Forwarded-For", "198.51.100.1"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Rate-Limit-Remaining", notNullValue()))
                .andExpect(header().string("X-Rate-Limit-Capacity", "50"));
    }

    @Test
    @DisplayName("Rate Limiting Filter - Returns 429 Too Many Requests when exhausted")
    void request_Returns429WhenLimitExceeded() throws Exception {
        String testIp = "203.0.113.42";

        // Exhaust token bucket
        for (int i = 0; i < 50; i++) {
            mockMvc.perform(get("/api/v1/employees").header("X-Forwarded-For", testIp))
                    .andExpect(status().isOk());
        }

        // 51st request should be throttled
        mockMvc.perform(get("/api/v1/employees").header("X-Forwarded-For", testIp))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"));
    }
}
