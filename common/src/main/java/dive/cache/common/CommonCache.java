package dive.cache.common;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 一个用于缓存键值对数据的对象
 * @author dawn
 * @date 2019/01/27 13:18
 * @param <K> 键的类型
 * @param <V> 值的类型
 */
public interface CommonCache<K, V> {

    /**
     * 新增或更新一个键值对
     * @param key 键
     * @param value 值
     */
    void set(K key, V value);

    /**
     * 是否存在某个键
     * @param key 键
     * @return 存在返回 true, 不存在返回 false
     */
    boolean has(K key);

    /**
     * 获取键对应的值
     * @param key 键
     * @return 存在返回对应的值, 不存在返回 null
     */
    V get(K key);


    /**
     * 删除对应键值对
     * @param key 键
     * @return 若存在返回值, 不存在返回 null
     */
    V delete(K key);

    /**
     * 移除对应键值对
     * @param key 键
     */
    void remove(K key);

    /**
     * 清除所有键值对
     */
    void clear();


    /**
     * 若不存在则存入
     * @param key 键
     * @param value 值
     * @return 值
     */
    default V putIfAbsent(K key, V value) {
        V v = this.get(key);
        if (v == null) {
            this.set(key, value);
            v = value;
        }
        return v;
    }

    /**
     * 获取对应值，若无则返回默认值
     * @param key key
     * @param defaultValue 默认值
     * @return 值
     */
    default V getOrDefault(K key, V defaultValue) {
        V v;
        return ((v = this.get(key)) != null) ? v : defaultValue;
    }

    /**
     * 获取或生成
     * @param key 键
     * @param supplier 生产者
     * @return 值
     */
    default V get(K key, Supplier<V> supplier) {
        V v = this.get(key);
        if (null == v) {
            Objects.requireNonNull(supplier, "supplier");
            this.set(key, (v = supplier.get()));
        }
        return v;
    }

    /**
     * 获取或映射一个
     * @param key 键
     * @param mapping 映射
     * @return 值
     */
    default V computeIfAbsent(K key, Function<? super K, ? extends V> mapping) {
        Objects.requireNonNull(mapping);
        V v, newValue;
        return ((v = this.get(key)) == null &&
                (newValue = mapping.apply(key)) != null &&
                (v = this.putIfAbsent(key, newValue)) == null) ? newValue : v;
    }

    /**
     * 若存在则重新映射一个
     * @param key 键
     * @param remapping 重新映射
     * @return 值
     */
    default V computeIfPresent(K key,
                               BiFunction<? super K, ? super V, ? extends V> remapping) {
        Objects.requireNonNull(remapping);
        V oldValue = this.get(key);
        Objects.requireNonNull(oldValue);
        V newValue = remapping.apply(key, oldValue);
        if (null != newValue) {
            this.set(key, newValue);
            return newValue;
        } else {
            this.remove(key);
        }
        return null;
    }

    /**
     * 重新映射一个
     * @param key 键
     * @param remapping 重新映射
     * @return 值
     */
    default V compute(K key,
                      BiFunction<? super K, ? super V, ? extends V> remapping) {
        Objects.requireNonNull(remapping);
        V newValue = remapping.apply(key, this.get(key));
        if (null != newValue) {
            this.set(key, newValue);
        }
        return newValue;
    }

    /**
     * 合并计算
     * @param key 键
     * @param value 被计算的
     * @param remapping 计算映射
     * @return 值
     */
    default V merge(K key, V value,
                    BiFunction<? super V, ? super V, ? extends V> remapping) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(remapping);
        V newValue = remapping.apply(this.get(key), value);
        if (null != newValue) {
            this.set(key, newValue);
        }
        return newValue;
    }

}
