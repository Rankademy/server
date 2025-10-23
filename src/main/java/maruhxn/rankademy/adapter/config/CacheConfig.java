package maruhxn.rankademy.adapter.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        List<CaffeineCache> caches = List.of(
                new CaffeineCache("ddragonLatestVersion", Caffeine.newBuilder()
                        .expireAfterWrite(1, TimeUnit.DAYS)
                        .build())
        );

        cacheManager.setCaches(caches);
        return cacheManager;
    }
}