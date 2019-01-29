package mime.cache.mime;

import java.util.Map;

/**
 * 利用Stream遍历
 * @author dawn
 * @param <K> 键
 * @param <V> 值
 */
public class Pair<K , V> implements Map.Entry<K, V> {

    private K key;
    private V value;

    Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        V exist = this.value;
        this.value = value;
        return exist;
    }
}
