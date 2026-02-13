package com.cm.sanchalak.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine cache configuration
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = 
            new SimpleCacheManager();
        
        // Create caches with specific configurations
        List<Cache> caches = new ArrayList<>();
        
        // Parent-linkage cache: 1 hour TTL
        caches.add(new CaffeineCache(
            "parent-linkage",
            Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(5000)
                .recordStats()
                .build()
        ));
        
        // Route-assignments cache: 6 hour TTL (transport assignments change infrequently)
        caches.add(new CaffeineCache(
            "route-assignments",
            Caffeine.newBuilder()
                .expireAfterWrite(6, TimeUnit.HOURS)
                .maximumSize(10000)
                .recordStats()
                .build()
        ));
        
        // Fee configs (Structures, Categories): 24 hour TTL (very infrequent changes)
        caches.add(new CaffeineCache(
            "fee-structures",
            Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .maximumSize(1000)
                .recordStats()
                .build()
        ));
        
        caches.add(new CaffeineCache(
            "fee-categories",
            Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .maximumSize(500)
                .recordStats()
                .build()
        ));
        
        cacheManager.setCaches(caches);
        return cacheManager;
    }
}
