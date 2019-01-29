package mime.cache.ehcache;

import dive.cache.common.CommonCache;
import org.ehcache.Cache;

/**
 * 一个用于缓存键值对数据的对象, 利用ehcache实现
 * 需引入包 'org.ehcache:ehcache:3.6.2'
 * @author dawn
 * @param <K> 键的类型
 * @param <V> 值的类型
 */
public class EhcacheCache<K, V> implements CommonCache<K, V> {

    /**
     * 实现ehcache中Cache接口对象, 用于实现缓存功能
     *
     * 例1:
     *  UserManagedCache<Long, String> cache =
     *      UserManagedCacheBuilder.newUserManagedCacheBuilder(Long.class, String.class)
     *          .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(60)))
     *          .build(false);
     *  userManagedCache.init();
     *
     * 例2:
     *  CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
     *      .with(CacheManagerBuilder.persistence(".ehcache_persistence"))
     *      .withCache("persistent-long-string",
     *          CacheConfigurationBuilder.newCacheConfigurationBuilder(
     *              Long.class, String.class,
     *              ResourcePoolsBuilder.newResourcePoolsBuilder()
     *                  .heap(10, EntryUnit.ENTRIES)
     *                  .disk(10, MemoryUnit.MB, true)))
     *      .build();
     *  cacheManager.init();
     *  Cache<Long, String> cache = cacheManager.getCache("persistent-long-string", Long.class, String.class);
     *
     * 例3:
     *  CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
     *  cacheManager.init();
     *  Cache<Long, String> cache = ehcacheManager.createCache("long-string",
     *      CacheConfigurationBuilder.newCacheConfigurationBuilder(
     *          Long.class, String.class, ResourcePoolsBuilder.heap(10))
     *      .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(60))));
     */
    private final Cache<K, V> cache;

    /**
     * 构造器
     * @param cache ehcache的Cache实例
     */
    public EhcacheCache(Cache<K, V> cache) {
        this.cache = cache;
    }

    public Cache<K, V> getCache() {
        return cache;
    }


    @Override
    public void set(K key, V value) {
        cache.put(key, value);
    }


    @Override
    public boolean has(K key) {
        return cache.containsKey(key);
    }

    @Override
    public V get(K key) {
        return cache.get(key);
    }


    @Override
    public V delete(K key) {
        V value = cache.get(key);
        cache.remove(key);
        return value;
    }

    @Override
    public void remove(K key) {
        cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

}
