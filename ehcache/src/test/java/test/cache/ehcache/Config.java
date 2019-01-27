package test.cache.ehcache;

import org.ehcache.CacheManager;
import org.ehcache.UserManagedCache;
import org.ehcache.config.builders.*;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Config {
//    @Bean
    public CacheManager persistentEhcacheManager(){
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .with(CacheManagerBuilder.persistence(".ehcache_persistence"))
                .withCache("persistent-long-unique",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(
                                Long.class, Unique.class,
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        .heap(10, EntryUnit.ENTRIES)
                                        .disk(10, MemoryUnit.MB, true)))
                .build();
        cacheManager.init();
        return cacheManager;
    }

    @Bean
    public CacheManager ehcacheManager(){
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
//                .withCache("long-unique",
//                        CacheConfigurationBuilder.newCacheConfigurationBuilder(
//                                Long.class, Unique.class,
//                                ResourcePoolsBuilder.heap(10)))
                .build();
        cacheManager.init();
        return cacheManager;
    }

    public UserManagedCache<Long, Unique> userManagerCache() {
        UserManagedCache<Long, Unique> userManagedCache =
            UserManagedCacheBuilder.newUserManagedCacheBuilder(Long.class, Unique.class)
                    .withExpiry(ExpiryPolicyBuilder.
                            timeToLiveExpiration(Duration.ofSeconds(60)))
                .build(false);
        userManagedCache.init();
        return userManagedCache;
    }

}
