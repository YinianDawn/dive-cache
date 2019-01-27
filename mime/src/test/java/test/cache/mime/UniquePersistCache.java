package test.cache.mime;

import mime.cache.mime.PersistCache;
import org.springframework.stereotype.Component;

@Component
public class UniquePersistCache extends PersistCache<Integer, Unique> {


    public UniquePersistCache() {
        super();
    }
}
