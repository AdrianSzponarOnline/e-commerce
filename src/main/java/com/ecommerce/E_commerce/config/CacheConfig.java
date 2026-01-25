package com.ecommerce.E_commerce.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Arrays;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager(){
        SimpleCacheManager manager = new SimpleCacheManager();

        manager.setCaches(Arrays.asList(
                buildCache("categories", Duration.ofHours(24), 100),

                buildCache("attributes", Duration.ofHours(24), 500),

                buildCache("products", Duration.ofMinutes(30), 2000),

                buildCache("product_lists", Duration.ofMinutes(30), 3000),

                buildCache("product_images", Duration.ofHours(24), 2000),

                buildCache("product_counts", Duration.ofHours(24), 3000),

                buildCache("category_attributes", Duration.ofHours(24), 500),

                buildCache("product_attributes",Duration.ofHours(24) , 3000),

                buildCache("ai_context_attributes", Duration.ofHours(24), 500),

                buildCache("pages", Duration.ofHours(24), 500),

                buildCache("shop_settings", Duration.ofHours(24), 500),

                buildCache("faq_items", Duration.ofHours(24), 500),

                buildCache("footer_data", Duration.ofHours(24), 500)

        ));
        return manager;
    }


    private CaffeineCache buildCache(String name, Duration ttl, long maxSize) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(maxSize)
                .recordStats()
                .build());
    }
}
