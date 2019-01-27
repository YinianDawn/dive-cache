package test.cache.guava;

import com.google.common.cache.LoadingCache;
import mime.cache.guava.GuavaCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniqueCache extends GuavaCache<Integer, Unique> {

    @Autowired
    public UniqueCache(LoadingCache cache) {
        super(cache);
    }
}
