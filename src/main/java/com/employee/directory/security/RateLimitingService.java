/*
 * security/RateLimitingService.java
 * Component managing per-client token buckets for API throttling.
 * Connects to: security/RateLimitingFilter.java, Bucket4j Core
 * Created: 2026-08-08
 */
package com.employee.directory.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing client request token buckets using Bucket4j algorithm.
 */
@Service
public class RateLimitingService {

    private static final int BUCKET_CAPACITY = 50; // Max requests
    private static final int REFILL_TOKENS = 50;
    private static final Duration REFILL_DURATION = Duration.ofMinutes(1);

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Resolves or creates a Bucket for the given client IP address.
     * 
     * @param clientIp Client IP address.
     * @return Bucket instance.
     */
    public Bucket resolveBucket(String clientIp) {
        return cache.computeIfAbsent(clientIp, k -> createNewBucket());
    }

    /**
     * Attempts to consume 1 token for the specified client key.
     * 
     * @param key Client IP or user key.
     * @return true if token was consumed, false if bucket is empty.
     */
    public boolean tryConsume(String key) {
        Bucket bucket = resolveBucket(key);
        return bucket.tryConsume(1);
    }

    /**
     * Returns remaining available tokens for the specified key.
     * 
     * @param key Client IP or user key.
     * @return Available tokens.
     */
    public long getAvailableTokens(String key) {
        return resolveBucket(key).getAvailableTokens();
    }

    private Bucket createNewBucket() {
        Refill refill = Refill.intervally(REFILL_TOKENS, REFILL_DURATION);
        Bandwidth limit = Bandwidth.classic(BUCKET_CAPACITY, refill);
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
