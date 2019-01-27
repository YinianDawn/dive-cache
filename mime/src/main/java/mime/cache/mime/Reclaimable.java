package mime.cache.mime;

/**
 * 实现该接口回收空间的方法
 * @author dawn
 * @date 2019/01/27 17:31
 */
public interface Reclaimable {
    /**
     * 回收空间
     */
    void reclaim();
}
