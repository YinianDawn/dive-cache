package dive.cache.mime;

import java.io.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 一个用于缓存键值对数据的对象, 可以对键值对设置存活时间, 利用java原生Map和序列化实现
 * @author dawn
 * @param <K> 键的类型，需实现序列化接口，利用hashCode持久化，必须保证
 * @param <V> 值的类型，需实现序列化接口
 */
public class PersistCache<K extends Serializable, V extends Serializable>
        implements dive.cache.common.PersistCache<K, V>, Reclaimable {

    /**
     * 定时器单例
     */
    private final CacheUtil cacheUtil;

    /**
     * 序列化键的后缀名
     */
    private static final String SUFFIX_KEY = "_key";

    /**
     * 序列化过期时间的后缀名
     */
    private static final String SUFFIX_EXPIRE = "_expire";

    /**
     * 每个对象存储路径集合，不允许重复
     */
    private static final CopyOnWriteArraySet<String> PATHS = new CopyOnWriteArraySet<>();

    /**
     * 存储键值数据
     */
    private final ConcurrentHashMap<K, V> values = new ConcurrentHashMap<>();

    /**
     * 存储过期时间
     */
    private final ConcurrentHashMap<K, Long> expires = new ConcurrentHashMap<>();

    /**
     * 本缓存对象存储路径
     */
    private String path;

    /**
     * 默认情况下，键的持久化名称，每个键的转换结果应当唯一
     */
    private Function<K, String> name = k -> String.valueOf(k.hashCode());

    /**
     * 已持久化的键
     */
    private CopyOnWriteArraySet<K> keys = new CopyOnWriteArraySet<>();

    /**
     * 错误日志
     */
    private Consumer<String> error = System.err::println;

    /**
     * 设置输出日志
     * @param error 日志输出
     * @return 本实例
     */
    public PersistCache<K, V> error(Consumer<String> error) {
        Objects.requireNonNull(error, "error log");
        this.error = error;
        return this;
    }

    /**
     * 生成持久化文件名
     * @param key 键
     * @return 文件名
     */
    private String name(K key) {
        return this.name.apply(key);
    }

    /**
     * 序列化对象
     * @param file 文件对象
     * @param o 序列化对象
     */
    private void write(File file, Serializable o) {
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    this.error.accept("createNewFile " + file.getPath() + " failed");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file))) {
            os.writeObject(o);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 反序列化对象
     * @param name 文件名
     * @return 对象
     */
    @SuppressWarnings("unchecked")
    private static <T> T read(String name) {
        File file = new File(name);
        if (!file.exists()) {
            return null;
        }
        try (ObjectInputStream is = new ObjectInputStream(new FileInputStream(file))) {
            return (T) is.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 删除持久化
     * @param name 带有路径的文件名
     */
    private void delete(String name) {
        new File(name).deleteOnExit();
        new File(name + SUFFIX_KEY).deleteOnExit();
        new File(name + SUFFIX_EXPIRE).deleteOnExit();
    }

    /**
     * 移除
     * @param key 键
     */
    private void vanish(K key) {
        this.expires.remove(key);
        this.values.remove(key);
        if (this.keys.contains(key)) {
            // 若有持久化，则删除
            this.keys.remove(key);
            this.delete(this.path + "/" + name(key));
        }
    }

    /**
     * 持久化值
     * @param name 文件名
     * @param value 值
     */
    private void writeValue(String name, V value) {
        this.write(new File(this.path + "/" + name), value);
    }

    /**
     * 持久化键
     * @param name 文件名
     * @param key 键
     */
    private void writeKey(String name, K key) {
        this.write(new File(this.path + "/" + name + SUFFIX_KEY), key);
    }

    /**
     * 持久化过期时间
     * @param name 文件名
     * @param expire 过期时间, 毫秒
     */
    private void writeExpire(String name, Long expire) {
        this.write(new File(this.path + "/" + name + SUFFIX_EXPIRE), expire);
    }

    /**
     * 获取过期时间
     * @param key 键
     * @return 过期时间
     */
    private Long getExpire(K key) {
        Long expire = this.expires.get(key);
        if (CacheUtil.alive(expire)) {
            // 如果内存里有，直接返回内存里的
            return expire;
        }

        if (!this.keys.contains(key)) {
            // 如果键没有进行持久化，则不存在键
            return null;
        }

        // 持久化文件名
        String name = this.name(key);
        expire = PersistCache.read(this.path + "/" + name + SUFFIX_EXPIRE);
        boolean disappear = true;
        if (CacheUtil.alive(expire)) {
            try {
                // 获取值
                V value = PersistCache.read(this.path + "/" + name);
                if (null != value) {
                    // 若值也存在，缓存入内存
                    this.values.put(key, value);
                    this.expires.put(key, expire);
                    disappear = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (disappear) {
            this.vanish(key);
            expire = null;
        }
        return expire;
    }

    /**
     * 获取值
     * @param key 键
     * @return 值
     */
    private V getValue(K key) {
        if (!CacheUtil.alive(this.getExpire(key))) {
            return null;
        }

        V value = this.values.get(key);
        if (null != value) {
            return value;
        }

        if (!this.keys.contains(key)) {
            return null;
        }

        String name = this.name(key);
        value = PersistCache.read(this.path + "/" + name);
        boolean disappear = true;
        if (null != value) {
            // 若存在值，则进一步判断过期时间
            try {
                Long expire = PersistCache.read(this.path + "/" + name + SUFFIX_EXPIRE);
                if (CacheUtil.alive(expire)) {
                    // 未过期，存入内存
                    this.values.put(key, value);
                    this.expires.put(key, expire);
                    disappear = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (disappear) {
            this.vanish(key);
            value = null;
        }
        return value;
    }

    /**
     * 设置或更新一个键值对
     * @param key 键
     * @param value 值
     * @param expire 过期时间, 毫秒
     */
    private void store(K key, V value, Long expire) {
        Long e = this.getExpire(key);
        if (null != e) {
            String name = this.name(key);
            this.writeValue(name, value);
            this.values.put(key, value);
            if (!e.equals(expire)) {
                this.writeExpire(name, expire);
                this.expires.put(key, expire);
            }
            return;
        }
        this.values.put(key, value);
        this.expires.put(key, expire);
        this.write(key, value, expire);
    }

    /**
     * 持久化
     * @param key 键
     * @param value 值
     * @param expire 过期时间, 毫秒
     */
    private void write(K key, V value, Long expire) {
        this.keys.add(key);
        String name = this.name(key);
        this.writeValue(name, value);
        this.writeKey(name, key);
        this.writeExpire(name, expire);
    }

    /**
     * 清空所有数据
     */
    private void empty() {
        this.expires.clear();
        this.values.clear();
        this.keys.stream()
                .map(k -> this.path + "/" + name(k))
                .forEach(this::delete);
        this.keys.clear();
    }

    /**
     * 回收空间
     */
    @Override
    public void reclaim() {
        Stream.concat(this.expires.keySet().stream(), this.keys.stream())
                .distinct() // 所有的键
                .parallel()
                // 若程序结束，则不用清理
                .filter(k -> this.cacheUtil.isAlive())
                .forEach(this::reclaim);
    }

    /**
     * 尝试清理某个键
     * @param key 键
     */
    private void reclaim(K key) {
        if (!CacheUtil.alive(this.getExpire(key))) {
            this.vanish(key);
        }
    }

    /**
     * 遍历所有键值对
     * @param action 遍历函数
     */
    public void forEach(BiConsumer<K, V> action) {
        Stream.concat(this.expires.keySet().stream(), this.keys.stream())
                .distinct()
                .parallel()
                .filter(this::has)
                .forEach(k -> action.accept(k, get(k)));
    }

    /**
     * 将所有缓存键值对数据变成流
     * @return 流
     */
    public Stream<Map.Entry<K, V>> stream() {
        return Stream.concat(this.expires.keySet().stream(), this.keys.stream())
                .distinct()
                .parallel()
                .filter(this::has)
                .map(k -> new Pair<>(k, get(k)));
    }

    /**
     * 有效的缓存个数
     * @return 缓存个数
     */
    public int size() {
        return (int) Stream.concat(this.expires.keySet().stream(), this.keys.stream())
                .distinct()
                .map(this::getExpire)
                .filter(CacheUtil::alive)
                .count();
    }


    // ------------------------------------------------

    /**
     * 构造器
     */
    public PersistCache() {
        this("default");
    }

    /**
     * 构造器
     * @param alias 缓存别名, 也是缓存路径下的下级文件夹名称
     */
    public PersistCache(String alias) {
        this(".mime_cache", alias);
    }

    /**
     * 构造器
     * @param path 缓存路径
     * @param alias 缓存别名, 也是缓存路径下的下级文件夹名称
     */
    public PersistCache(String path, String alias) {
        this(path, alias, null, CacheUtil.delay, CacheUtil.period);
    }

    /**
     * 构造器
     * @param path 缓存路径
     * @param alias 缓存别名, 也是缓存路径下的下级文件夹名称
     * @param name 键转字符串函数
     * @param delay 清理任务延时时间，毫秒
     * @param period 清理任务周期，毫秒
     */
    public PersistCache(String path, String alias, Function<K, String> name, long delay, long period) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(alias, "alias");
        path = path.trim() + "/" + alias.trim();
        File file = new File(path);
        if (!file.exists() && !file.mkdirs()) {
            throw new RuntimeException("make dir '" + path + "' fail");
        }
        if (!file.isDirectory()) {
            throw new RuntimeException("'" + path + "' is not directory");
        }
        if (PATHS.contains(path)) {
            throw new RuntimeException("dir '" + path + "' is used");
        }

        cacheUtil = CacheUtil.getInstance(delay, period);

        if (null != name) {
            this.name = name;
        }
        this.path = path;
        String[] list = file.list();
        if (null != list) {
            Set<String> set = Arrays.stream(list).collect(Collectors.toSet());
            for (String n : set) {
                String nk = n + SUFFIX_KEY;
                String ne = n + SUFFIX_EXPIRE;
                if (set.contains(nk) && set.contains(ne)) {
                    K key = PersistCache.read(path + "/" + nk);
                    if (null != key) {
                        this.keys.add(key);
                    } else {
                        this.delete(path + "/" + n);
                    }
                }
            }
        }
        PATHS.add(path);
        this.cacheUtil.add(this);
    }



    @Override
    public void set(K key, V value) {
        this.store(key, value, -1L);
    }

    @Override
    public boolean has(K key) {
        return CacheUtil.alive(this.getExpire(key));
    }

    @Override
    public V get(K key) {
        return this.getValue(key);
    }

    @Override
    public V delete(K key) {
        V value = this.getValue(key);
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
        this.store(key, value, CacheUtil.now() + alive);
    }

    @Override
    public boolean has(K key, Instant expire) {
        boolean exist = this.has(key);
        if (exist && this.keys.contains(key)) {
            Long e = expire.toEpochMilli();
            this.writeExpire(this.name(key), e);
            this.expires.put(key, e);
        }
        return exist;
    }

    @Override
    public boolean has(K key, long timeout, TimeUnit unit) {
        boolean exist = this.has(key);
        if (exist && this.keys.contains(key)) {
            Long e = CacheUtil.expire(timeout, unit);
            this.writeExpire(this.name(key), e);
            this.expires.put(key, e);
        }
        return exist;
    }

    @Override
    public boolean has(K key, long alive) {
        boolean exist = this.has(key);
        if (exist && this.keys.contains(key)) {
            Long e = CacheUtil.now() + alive;
            writeExpire(name(key), e);
            expires.put(key, e);
        }
        return exist;
    }

    @Override
    public V get(K key, Instant expire) {
        V value = this.get(key);
        if (null != value && this.keys.contains(key)) {
            Long e = expire.toEpochMilli();
            this.writeExpire(name(key), e);
            this.expires.put(key, e);
        }
        return value;
    }

    @Override
    public V get(K key, long timeout, TimeUnit unit) {
        V value = this.get(key);
        if (null != value && this.keys.contains(key)) {
            Long e = CacheUtil.expire(timeout, unit);
            this.writeExpire(this.name(key), e);
            this.expires.put(key, e);
        }
        return value;
    }

    @Override
    public V get(K key, long alive) {
        V value = this.get(key);
        if (null != value && this.keys.contains(key)) {
            Long e = CacheUtil.now() + alive;
            this.writeExpire(this.name(key), e);
            this.expires.put(key, e);
        }
        return value;
    }


    @Override
    public long expire(K key) {
        Long expire = this.getExpire(key);
        return null != expire ? expire : 0;
    }

    @Override
    public long last(K key) {
        Long expire = this.getExpire(key);
        return null != expire ? expire - CacheUtil.now() : 0;
    }


    @Override
    public boolean persist(K key, V value) {
        this.set(key, value);
        return true;
    }

    @Override
    public boolean persist(K key) {
        V value = this.get(key);
        if (null == value) {
            return false;
        }
        this.set(key, value);
        return true;
    }

}
