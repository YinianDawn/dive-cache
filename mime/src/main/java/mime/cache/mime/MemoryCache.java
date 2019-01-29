package mime.cache.mime;

import dive.cache.common.TimeCache;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * 一个用于缓存键值对数据的对象, 可以对键值对设置存活时间, 利用java原生Map实现
 * @author dawn
 * @param <K> 键的类型
 * @param <V> 值的类型
 */
public class MemoryCache<K, V> implements TimeCache<K, V>, Reclaimable {

    /**
     * 定时器单例
     */
    private final CacheUtil cacheUtil;

    /**
     * 存储键值数据
     */
    private final ConcurrentHashMap<K, V> values = new ConcurrentHashMap<>();

    /**
     * 存储过期时间
     */
    private final ConcurrentHashMap<K, Long> expires = new ConcurrentHashMap<>();

    /**
     * 设置或更新一个键值对
     * @param key 键
     * @param value 值
     * @param expire 过期时间
     */
    private void store(K key, V value, Long expire) {
        this.values.put(key, value);
        this.expires.put(key, expire);
    }

    /**
     * 移除一个键
     * @param key 键
     */
    private void vanish(K key) {
        this.expires.remove(key);
        this.values.remove(key);
    }

    /**
     * 情况所有数据
     */
    private void empty() {
        this.expires.clear();
        this.values.clear();
    }

    @Override
    public void reclaim() {
        this.expires.keySet().parallelStream()
                // 若程序结束，则不用清理
                .filter(k -> cacheUtil.isAlive())
                .forEach(this::reclaim);
    }

    /**
     * 尝试清理某个键
     * @param key 键
     */
    private void reclaim(K key) {
        if (!CacheUtil.alive(this.expires.get(key))) {
            this.vanish(key);
        }
    }

    /**
     * 遍历所有键值对
     * @param action 遍历函数
     */
    public void forEach(BiConsumer<K, V> action) {
        this.expires.keySet().parallelStream()
                .filter(this::has)
                .forEach(k -> action.accept(k, get(k)));
    }

    /**
     * 将所有缓存键值对数据变成流
     * @return 流
     */
    public Stream<Pair<K, V>> stream() {
        return this.expires.keySet().parallelStream()
                .filter(this::has)
                .map(k -> new Pair<>(k, get(k)));
    }

    /**
     * 有效的缓存个数
     * @return 缓存个数
     */
    public int size() {
        return (int) this.expires.values().stream()
                .filter(CacheUtil::alive)
                .count();
    }

    // 上面是基础方法 ------------------------------------------------

    /**
     * 构造器
     * @param delay 清理任务延时时间，毫秒
     * @param period 清理任务周期，毫秒
     */
    public MemoryCache(long delay, long period) {
        this.cacheUtil = CacheUtil.getInstance(delay, period);
        this.cacheUtil.add(this);
    }

    /**
     * 构造器
     */
    public MemoryCache() {
        this.cacheUtil = CacheUtil.getInstance();
        this.cacheUtil.add(this);
    }


    @Override
    public void set(K key, V value) {
        this.store(key, value, -1L);
    }


    @Override
    public boolean has(K key) {
        return CacheUtil.alive(this.expires.get(key));
    }

    @Override
    public V get(K key) {
        return this.has(key) ? this.values.get(key) : null;
    }


    @Override
    public V delete(K key) {
        V value = this.get(key);
        this.vanish(key);
        return value;
    }

    @Override
    public void remove(K key) {
        this.vanish(key);
    }

    @Override
    public void clear() {
        this.empty();
    }


    @Override
    public void set(K key, V value, Instant expire) {
        this.store(key, value, expire.toEpochMilli());
    }

    @Override
    public void set(K key, V value, long timeout, TimeUnit unit) {
        this.store(key, value, CacheUtil.expire(timeout, unit));
    }

    @Override
    public void set(K key, V value, long alive) {
        this.store(key, value, alive < 0 ? -1L : CacheUtil.now() + alive);
    }


    @Override
    public boolean has(K key, Instant expire) {
        boolean exist = this.has(key);
        if (exist) {
            this.expires.put(key, expire.toEpochMilli());
        }
        return exist;
    }

    @Override
    public boolean has(K key, long timeout, TimeUnit unit) {
        boolean exist = this.has(key);
        if (exist) {
            this.expires.put(key, CacheUtil.expire(timeout, unit));
        }
        return exist;
    }

    @Override
    public boolean has(K key, long alive) {
        boolean exist = this.has(key);
        if (exist) {
            this.expires.put(key, alive < 0 ? -1L : CacheUtil.now() + alive);
        }
        return exist;
    }


    @Override
    public V get(K key, Instant expire) {
        V value = this.get(key);
        if (null != value) {
            this.expires.put(key, expire.toEpochMilli());
        }
        return value;
    }

    @Override
    public V get(K key, long timeout, TimeUnit unit) {
        V value = this.get(key);
        if (null != value) {
            this.expires.put(key, CacheUtil.expire(timeout, unit));
        }
        return value;
    }

    @Override
    public V get(K key, long alive) {
        V value = this.get(key);
        if (null != value) {
            this.expires.put(key, alive < 0 ? -1L : CacheUtil.now() + alive);
        }
        return value;
    }


    @Override
    public long expire(K key) {
        Long expire = this.expires.get(key);
        return null != expire ? expire : 0;
    }

    @Override
    public long last(K key) {
        Long expire = this.expires.get(key);
        return null != expire ? expire - CacheUtil.now() : 0;
    }

}
