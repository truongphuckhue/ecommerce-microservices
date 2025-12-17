package com.ecommerce.auth.security;

import com.ecommerce.auth.common.exception.RateLimitExceededException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Rate Limit Service Tests")
public class RateLimitServiceTest {
    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String TEST_KEY = "test_key";

    @BeforeEach
    @AfterEach
    void cleanup() {
        // Clean up Redis
        redisTemplate.delete("rate_limit:" + TEST_KEY);
    }

    @Test
    @DisplayName("Should allow requests within limit")
    void testAllowWithinLimit() {
        // Should allow 5 requests
        for (int i = 0; i < 5; i++) {
            assertDoesNotThrow(() ->
                    rateLimitService.checkRateLimit(TEST_KEY, 5, 60, "Rate limit exceeded")
            );
        }
    }

    @Test
    @DisplayName("Should block requests exceeding limit")
    void testBlockExceedingLimit() {
        // First 5 should pass
        for (int i = 0; i < 5; i++) {
            rateLimitService.checkRateLimit(TEST_KEY, 5, 60, "Rate limit exceeded");
        }

        // 6th should fail
        RateLimitExceededException exception = assertThrows(
                RateLimitExceededException.class,
                () -> rateLimitService.checkRateLimit(TEST_KEY, 5, 60, "Rate limit exceeded")
        );

        assertTrue(exception.getMessage().contains("Rate limit exceeded"));
        assertTrue(exception.getMessage().contains("Try again in"));
    }

    @Test
    @DisplayName("Should reset rate limit")
    void testResetRateLimit() {
        // Use up all attempts
        for (int i = 0; i < 5; i++) {
            rateLimitService.checkRateLimit(TEST_KEY, 5, 60, "Rate limit exceeded");
        }

        // Reset
        rateLimitService.resetRateLimit(TEST_KEY);

        // Should allow again
        assertDoesNotThrow(() ->
                rateLimitService.checkRateLimit(TEST_KEY, 5, 60, "Rate limit exceeded")
        );
    }
}
