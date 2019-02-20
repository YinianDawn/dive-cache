package test.cache.ehcache;

import dive.cache.ehcache.EhcacheCache;
import org.ehcache.CacheManager;
import org.ehcache.UserManagedCache;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class UniqueCache extends EhcacheCache<Long, Unique> {

    @Autowired
    public UniqueCache(CacheManager ehcacheManager) {
        super(ehcacheManager.createCache("long-unique",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        Long.class, Unique.class, ResourcePoolsBuilder.heap(10))
                .withExpiry(ExpiryPolicyBuilder.
                        timeToLiveExpiration(Duration.ofSeconds(60)))));
    }

    public UniqueCache(UserManagedCache<Long, Unique> userManagedCache) {
        super(userManagedCache);
    }

}
