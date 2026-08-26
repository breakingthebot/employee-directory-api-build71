/*
 * config/CacheConfig.java
 * Spring Cache configuration bean registering Caffeine cache manager and eviction rules.
 * Connects to: services/impl/EmployeeServiceImpl.java
 * Created: 2026-08-08
 */
package com.employee.directory.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Configuration class enabling Spring Caching and registering Caffeine cache instances.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_EMPLOYEES = "employees";
    public static final String CACHE_ANALYTICS = "employee-analytics";
    public static final String CACHE_LISTINGS = "employee-listings";

    /**
     * Configures the CaffeineCacheManager bean with TTL and maximum size settings.
     * 
     * @return CacheManager instance.
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheNames(List.of(CACHE_EMPLOYEES, CACHE_ANALYTICS, CACHE_LISTINGS));
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats());

        return cacheManager;
    }
}
