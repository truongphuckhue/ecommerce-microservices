package com.ecommerce.auth.security;

import com.ecommerce.auth.common.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Check if rate limit is exceeded
     *
     * @param key Rate limit key (e.g., "register:ip:192.168.1.1")
     * @param maxAttempts Maximum allowed attempts
     * @param windowSeconds Time window in seconds
     * @param errorMessage Error message if limit exceeded
     * @throws /*RateLimitExceededException if limit is exceeded
     */
    public void checkRateLimit(String key, int maxAttempts, int windowSeconds, String errorMessage) {
        String redisKey = "rate_limit:" + key;
        String value = redisTemplate.opsForValue().get(redisKey);

        if (value == null) {
            // First attempt in this window
            redisTemplate.opsForValue().set(redisKey, "1", windowSeconds, TimeUnit.SECONDS);
            log.debug("Rate limit initialized for key: {}", key);
            return;
        }

        int attempts = Integer.parseInt(value);

        if (attempts >= maxAttempts) {
            // Rate limit exceeded
            Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            String message = String.format("%s. Try again in %d seconds.", errorMessage, ttl);

            log.warn("Rate limit exceeded for key: {}. Attempts: {}/{}", key, attempts, maxAttempts);
            throw new RateLimitExceededException(message);
        }

        // Increment counter
        redisTemplate.opsForValue().increment(redisKey);
        log.debug("Rate limit check passed for key: {}. Attempts: {}/{}", key, attempts + 1, maxAttempts);
    }

    /**
     * Reset rate limit for a key
     */
    public void resetRateLimit(String key) {
        String redisKey = "rate_limit:" + key;
        redisTemplate.delete(redisKey);
        log.info("Rate limit reset for key: {}", key);
    }
}
