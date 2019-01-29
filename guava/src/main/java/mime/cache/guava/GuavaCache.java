package mime.cache.guava;

import com.google.common.cache.LoadingCache;
import dive.cache.common.CommonCache;

/**
 * 一个用于缓存键值对数据的对象, 利用ehcache实现
 * 需引入包 'com.google.guava:guava:27.0.1-jre'
 * @author dawn
 * @param <K> 键的类型
 * @param <V> 值的类型
 */
public class GuavaCache<K, V> implements CommonCache<K, V> {

    /**
     * 实现guava中LoadingCache接口对象, 用于实现缓存功能
     * 例:
     *  LoadingCache<K, String> cache = CacheBuilder.newBuilder()
     *      .maximumSize(10000)
     *      .expireAfterAccess(60, TimeUnit.SECONDS)
     *      .build(new CacheLoader<Integer, String>() {
     *          @ Override
     *          public String load(Integer key) throws Exception {
     *              return null;
     *          }
     *      });
     */
    private final LoadingCache<K, V> cache;

    /**
     * 构造器
     * @param cache guava的LoadingCache实例
     */
    public GuavaCache(LoadingCache<K, V> cache) {
        this.cache = cache;
    }

    public LoadingCache<K, V> getCache() {
        return cache;
    }


    @Override
    public void set(K key, V value) {
        cache.put(key, value);
    }


    @Override
    public boolean has(K key) {
        return null != cache.getIfPresent(key);
    }

    @Override
    public V get(K key) {
        return cache.getIfPresent(key);
    }


    @Override
    public V delete(K key) {
        V v = cache.getIfPresent(key);
        cache.invalidate(key);
        return v;
    }

    @Override
    public void remove(K key) {
        cache.invalidate(key);
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }

}
