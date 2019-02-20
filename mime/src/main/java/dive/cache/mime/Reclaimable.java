package dive.cache.mime;

/**
 * 实现该接口回收空间的方法
 * @author dawn
 */
public interface Reclaimable {
    /**
     * 回收空间
     */
    void reclaim();
}
