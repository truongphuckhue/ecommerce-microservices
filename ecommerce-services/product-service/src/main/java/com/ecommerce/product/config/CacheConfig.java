package com.ecommerce.product.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Products cache - 30 minutes
        cacheConfigurations.put("products", 
            RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30)));
        
        // Products list cache - 10 minutes
        cacheConfigurations.put("productsList", 
            RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)));
        
        // Categories cache - 1 hour
        cacheConfigurations.put("categories", 
            RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1)));
        
        // Category tree - 2 hours
        cacheConfigurations.put("categoryTree", 
            RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(2)));
        
        // Featured products - 1 hour
        cacheConfigurations.put("featuredProducts", 
            RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1)));
        
        // Best selling - 30 minutes
        cacheConfigurations.put("bestSellingProducts", 
            RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
