/*
 * security/RateLimitingFilter.java
 * HTTP request filter enforcing Bucket4j rate limits and setting rate limit headers.
 * Connects to: security/RateLimitingService.java, config/SecurityConfig.java
 * Created: 2026-08-08
 */
package com.employee.directory.security;

import com.employee.directory.dto.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter checking client request rates and aborting with HTTP 429 when throttled.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;
    private final ObjectMapper objectMapper;

    public RateLimitingFilter(RateLimitingService rateLimitingService, ObjectMapper objectMapper) {
        this.rateLimitingService = rateLimitingService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip rate limiting for static documentation resources or H2 console
        String path = request.getRequestURI();
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/h2-console")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIP(request);

        if (rateLimitingService.tryConsume(clientIp)) {
            long remaining = rateLimitingService.getAvailableTokens(clientIp);
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(remaining));
            response.setHeader("X-Rate-Limit-Capacity", "50");

            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("X-Rate-Limit-Remaining", "0");
            response.setHeader("Retry-After", "60");

            ApiErrorResponse error = new ApiErrorResponse(
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    "Too Many Requests",
                    "Rate limit exceeded. Maximum 50 requests per minute allowed."
            );

            objectMapper.writeValue(response.getWriter(), error);
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }
}
