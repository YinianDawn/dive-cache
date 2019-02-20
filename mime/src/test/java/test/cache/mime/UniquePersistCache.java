package test.cache.mime;

import dive.cache.mime.PersistCache;
import org.springframework.stereotype.Component;

@Component
public class UniquePersistCache extends PersistCache<Integer, Unique> {


    public UniquePersistCache() {
        super();
    }
}
