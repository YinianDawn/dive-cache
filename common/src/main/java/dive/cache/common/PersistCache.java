package dive.cache.common;

/**
 * 缓存键值对数据, 可以对键值对设置存活时间, 持久化保存
 * @author dawn
 * @param <K> 键的类型
 * @param <V> 值的类型
 */
public interface PersistCache<K, V> extends TimeCache<K, V> {

    /**
     * 设置或更新键值对, 并进行持久化
     * @param key 键
     * @param value 值
     * @return 是否持久化成功
     */
    boolean persist(K key, V value);

    /**
     * 对已存在的键值对进行持久化
     * @param key 键
     * @return 是否持久化成功, 若不存在, 返回 false
     */
    boolean persist(K key);

}
