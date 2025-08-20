package org.example.onlinestoreapi.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Config file for caching
 */
@Configuration
public class CacheConfig {

    @Bean
    public Caffeine<Object, Object> caffeineConfig(){
        return Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .initialCapacity(100)
                .maximumSize(1000)
                .recordStats();
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object,Object> caffeine){
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeine);
        cacheManager.setAsyncCacheMode(true);
        cacheManager.setCacheNames(java.util.Arrays.asList(
                "products","productsByCategory","productById",
                "categories","categoryById","orderCache","orderHistoryCache"
        ));
        return cacheManager;
    }

}
