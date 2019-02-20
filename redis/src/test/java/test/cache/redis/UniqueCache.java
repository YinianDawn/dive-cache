package test.cache.redis;

import dive.cache.redis.StringRedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class UniqueCache extends StringRedisCache<Unique> {

    private static final String PREFIX = "test:unique:token:";

    @Autowired
    public UniqueCache(RedisTemplate<String, Unique> redisTemplate) {
        super(redisTemplate, PREFIX);
    }
}
