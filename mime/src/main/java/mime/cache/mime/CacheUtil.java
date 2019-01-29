package mime.cache.mime;

import java.util.concurrent.*;

/**
 * 定时清除和公共方法
 * @author dawn
 */
class CacheUtil {

    /**
     * 单例对象
     */
    private static CacheUtil instance;

    /**
     * 定时清理任务延迟执行时间，毫秒
     */
    static long delay = 1000 * 60 * 3;

    /**
     * 定时任务执行周期，毫秒
     */
    static long period = 1000 * 60 * 7;

    /**
     * 所有需要进行回收空间的缓存对象
     */
    private final CopyOnWriteArraySet<Reclaimable> caches = new CopyOnWriteArraySet<>();

    /**
     * 回收空间定时器
     */
    private ScheduledExecutorService schedule = null;

    /**
     * 标记程序是否结束，不同状态下的回收方法不同
     */
    private boolean alive = true;

    private CacheUtil() {}

    /**
     * 设置延时，获取实例之前的最后一个有效
     * @param delay 延时时间，毫秒
     */
    private static void setDelay(long delay) {
        CacheUtil.delay = delay;
    }

    /**
     * 设置周期，获取实例之前的最后一个有效
     * @param period 周期，毫秒
     */
    private static void setPeriod(long period) {
        CacheUtil.period = period;
    }

    /**
     * 获取单例实例
     * @return 单例对象
     */
    static CacheUtil getInstance() {
        if (null == instance) {
            synchronized (CacheUtil.class) {
                if (null == instance) {
                    instance = new CacheUtil();
                    // 守护线程模式，主线程结束则结束，不阻挡程序结束
                    instance.schedule = new ScheduledThreadPoolExecutor(1, new ThreadPoolExecutor.DiscardPolicy());
                    instance.schedule.scheduleAtFixedRate(() -> instance.reclaim(), delay, period, TimeUnit.MILLISECONDS);
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        // 停止定时器
                        instance.schedule.shutdown();
                        // 改变状态
                        instance.alive = false;
                    }));
                }
            }
        }
        return instance;
    }


    /**
     * 获取单例实例
     * @param delay 延时
     * @param period 周期
     * @return 单例对象
     */
    static CacheUtil getInstance(long delay, long period) {
        setDelay(delay);
        setPeriod(period);
        return getInstance();
    }



    /**
     * 回收空间
     */
    private void reclaim() {
        caches.parallelStream().forEach(Reclaimable::reclaim);
    }

    void add(Reclaimable reclaimable) {
        caches.add(reclaimable);
    }

    boolean isAlive() {
        return alive;
    }

    /**
     * 当前时刻时间戳
     * @return 时间戳
     */
    static long now() {
        return System.currentTimeMillis();
    }

    /**
     * 计算过期时间
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 过期时间的时间戳
     */
    static long expire(long timeout, TimeUnit unit) {
        long time = now();
        if (null != unit) {
            switch (unit) {
                case MILLISECONDS: time += timeout; break;
                case SECONDS: time += timeout * 1000; break;
                case MINUTES: time += timeout * 1000 * 60; break;
                case HOURS: time += timeout * 1000 * 60 * 60; break;
                case DAYS: time += timeout * 1000 * 60 * 60 * 24; break;
                case MICROSECONDS: time += timeout / 1000; break;
                case NANOSECONDS: time += timeout / 1000000; break;
                default:
            }
        }
        return time;
    }

    /**
     * 给定过期时间是否存活
     * @param expire 过期时间戳
     * @return 是否存活
     */
    static boolean alive(Long expire) {
        return null != expire && (expire < 0 || now() < expire);
    }

}
