package dive.cache.redis;

import dive.cache.common.PersistCache;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 一个用于缓存键值对数据的对象, 可以对键值对设置存活时间, 利用redis实现
 * 仅保存键为字符串的形式，方便正则匹配
 * 需引入包 'org.springframework.boot:spring-boot-starter-data-redis:2.0.1.RELEASE'
 * @author dawn
 * @param <V> 值的类型, 需实现序列化接口
 */
public class StringRedisCache<V extends Serializable> implements PersistCache<String, V> {

    /**
     * RedisTemplate对象, 用于实现缓存功能
     * 例:
     *  @ Bean
     *  public RedisTemplate<String, String> getRedisTemplateUnique(RedisConnectionFactory factory){
     *      RedisTemplate<String, String> template = new RedisTemplate<>();
     *      template.setConnectionFactory(factory);
     *      return template;
     *  }
     */
    private final RedisTemplate<String, V> cache;
    /**
     * 该实例化对象缓存键的前缀
     */
    private final String prefix;

    /**
     * 构造器
     * @param cache RedisTemplate对象，底层存取对象
     * @param prefix 前缀
     */
    public StringRedisCache(RedisTemplate<String, V> cache, String prefix) {
        this.cache = cache;
        this.prefix = prefix;
    }

    public RedisTemplate<String, V> getCache() {
        return cache;
    }

    /**
     * 存取前，用该方法封装key
     * @param key 原始key
     * @return 封装后的key，实际redis存取的key
     */
    protected String prefix(String key) {
        return null == prefix ? key : prefix + key;
    }


    @Override
    public void set(String key, V value) {
        cache.opsForValue().set(prefix(key), value);
    }


    @Override
    public boolean has(String key) {
        Boolean had = cache.hasKey(prefix(key));
        return null == had ? false : had;
    }

    @Override
    public V get(String key) {
        return cache.opsForValue().get(prefix(key));
    }


    @Override
    public V delete(String key) {
        key = prefix(key);
        V v = cache.opsForValue().get(key);
        cache.delete(key);
        return v;
    }

    @Override
    public void remove(String key) {
        cache.delete(prefix(key));
    }

    /**
     * 由于redis单线程原因，该操作耗时较大，不宜使用。
     * 缓存时候，应当设置过期时间，这样，redis会自动清理，而无需主动清除
     */
    @Override
    public void clear() {
        Set<String> keys = cache.keys(prefix("*"));
        if (null != keys) {
            cache.delete(keys);
        }
    }


    @Override
    public void set(String key, V value, Instant expire) {
        set(key, value, expire.toEpochMilli() - System.currentTimeMillis());
    }

    @Override
    public void set(String key, V value, long timeout, TimeUnit unit) {
        cache.opsForValue().set(prefix(key), value, timeout, unit);
    }

    @Override
    public void set(String key, V value, long alive) {
        if (0 < alive) {
            cache.opsForValue().set(prefix(key), value,
                    alive, TimeUnit.MILLISECONDS);
        }
    }


    @Override
    public boolean has(String key, Instant expire) {
        key = prefix(key);
        Boolean had = cache.hasKey(key);
        if (null != had && had && null != expire) {
            cache.expireAt(key, new Date(expire.toEpochMilli()));
        }
        return null == had ? false : had;
    }

    @Override
    public boolean has(String key, long timeout, TimeUnit unit) {
        String k = prefix(key);
        Boolean had = cache.hasKey(k);
        if (null != had && had && null != unit) {
            cache.expire(k, timeout, unit);
        }
        return null == had ? false : had;
    }

    @Override
    public boolean has(String key, long alive) {
        return has(key, alive, TimeUnit.MILLISECONDS);
    }


    @Override
    public V get(String key, Instant expire) {
        key = prefix(key);
        V v = cache.opsForValue().get(key);
        if (null != v && null != expire) {
            cache.expireAt(key, new Date(expire.toEpochMilli()));
        }
        return v;
    }

    @Override
    public V get(String key, long timeout, TimeUnit unit) {
        key = prefix(key);
        V v = cache.opsForValue().get(key);
        if (null != v && null != unit) {
            cache.expire(key, timeout, unit);
        }
        return v;
    }

    @Override
    public V get(String key, long alive) {
        return get(key, alive, TimeUnit.MILLISECONDS);
    }


    @Override
    public long expire(String key) {
        Long expire = cache.getExpire(prefix(key), TimeUnit.MILLISECONDS);
        return null == expire ? 0 : expire + System.currentTimeMillis();
    }

    @Override
    public long last(String key) {
        Long expire = cache.getExpire(prefix(key), TimeUnit.MILLISECONDS);
        return null == expire ? 0 : expire;
    }


    @Override
    public boolean persist(String key, V value) {
        key = prefix(key);
        cache.opsForValue().set(key, value);
        Boolean had = cache.persist(key);
        return null == had ? false : had;
    }

    @Override
    public boolean persist(String key) {
        Boolean had = cache.persist(prefix(key));
        return null == had ? false : had;
    }
}
