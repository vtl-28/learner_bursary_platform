package com.bursary.platform.Config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration using Caffeine
 * Improves performance by caching frequently accessed data
 */
@Configuration
@EnableCaching
public class CacheConfig {
    /**
     * Configure Caffeine cache manager
     * Caches:
     * - learnerProfile: Learner profile data (10 min expiry)
     * - bursaryList: Active bursaries list (5 min expiry)
     * - applicationsList: User applications (5 min expiry)
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "learnerProfile",
                "bursaryList",
                "applicationsList",
                "notifications"
        );

        cacheManager.setCaffeine(caffeineCacheBuilder());

        return cacheManager;
    }

    /**
     * Caffeine cache builder with custom configuration
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(1000)  // Maximum 1000 entries per cache
                .expireAfterWrite(10, TimeUnit.MINUTES)  // Expire after 10 minutes
                .recordStats();  // Enable cache statistics
    }



}
