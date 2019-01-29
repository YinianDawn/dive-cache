package dive.cache.common;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 缓存键值对数据, 可以对键值对设置存活时间
 * @author dawn
 * @param <K> 键的类型
 * @param <V> 值的类型
 */
public interface TimeCache<K, V> extends CommonCache<K, V> {

    /**
     * 新增或更新一个键值对
     * @param key 键
     * @param value 值
     * @param expire 过期时刻
     */
    void set(K key, V value, Instant expire);

    /**
     * 新增或更新一个键值对
     * @param key 键
     * @param value 值
     * @param timeout 超时时间
     * @param unit 时间单位
     */
    void set(K key, V value, long timeout, TimeUnit unit);

    /**
     * 新增或更新一个键值对
     * @param key 键
     * @param value 值
     * @param alive 存活毫秒数, 若为负数, 表明永不过期
     */
    void set(K key, V value, long alive);

    /**
     * 是否存在某个键
     * @param key 键
     * @param expire 若存在, 更新过期时刻
     * @return 存在返回 true, 不存在返回 false
     */
    boolean has(K key, Instant expire);

    /**
     * 是否存在某个键
     * @param key 键
     * @param timeout 若存在, 更新超时时间
     * @param unit 时间单位
     * @return 存在返回 true, 不存在返回 false
     */
    boolean has(K key, long timeout, TimeUnit unit);

    /**
     * 是否存在某个键
     * @param key 键
     * @param alive 若存在, 更新存活毫秒数, 若为负数, 表明永不过期
     * @return 存在返回 true, 不存在返回 false
     */
    boolean has(K key, long alive);


    /**
     * 获取键对应的值
     * @param key 键
     * @param expire 若存在, 更新过期时刻
     * @return 存在返回对应的值, 不存在返回 null
     */
    V get(K key, Instant expire);

    /**
     * 获取键对应的值
     * @param key 键
     * @param timeout 若存在, 更新超时时间
     * @param unit 时间单位
     * @return 存在返回对应的值, 不存在返回 null
     */
    V get(K key, long timeout, TimeUnit unit);

    /**
     * 获取键对应的值
     * @param key 键
     * @param alive 若存在, 更新存活毫秒数, 若为负数, 表明永不过期
     * @return 存在返回对应的值, 不存在返回 null
     */
    V get(K key, long alive);


    /**
     * 获取对应键的过期时间
     * @param key 键
     * @return 过期时间, 单位毫秒, 若不存在, 返回 0
     */
    long expire(K key);

    /**
     * 获取对应键的剩余存活时间
     * @param key 键
     * @return 剩余存活时间, 单位毫秒, 若不存在, 返回 0
     */
    long last(K key);


    /**
     * 若不存在则存入
     * @param key 键
     * @param value 值
     * @param alive 若存在, 更新存活毫秒数, 若为负数, 表明永不过期
     * @return 值
     */
    default V putIfAbsent(K key, V value, long alive) {
        V v = this.get(key);
        if (v == null) {
            this.set(key, value, alive);
            v = value;
        }
        return v;
    }

    /**
     * 获取或生成
     * @param key 键
     * @param supplier 生产者
     * @param alive 若存在, 更新存活毫秒数, 若为负数, 表明永不过期
     * @return 值
     */
    default V get(K key, Supplier<V> supplier, long alive) {
        V v = this.get(key);
        if (null == v) {
            Objects.requireNonNull(supplier, "supplier");
            this.set(key, (v = supplier.get()), alive);
        }
        return v;
    }

    /**
     * 获取或映射一个
     * @param key 键
     * @param mapping 映射
     * @param alive 若存在, 更新存活毫秒数, 若为负数, 表明永不过期
     * @return 值
     */
    default V computeIfAbsent(K key, Function<? super K, ? extends V> mapping, long alive) {
        Objects.requireNonNull(mapping);
        V v, newValue;
        return ((v = this.get(key)) == null &&
                (newValue = mapping.apply(key)) != null &&
                (v = this.putIfAbsent(key, newValue, alive)) == null) ? newValue : v;
    }

}
