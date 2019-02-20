package test.cache.mime;

import dive.cache.mime.MemoryCache;
import org.springframework.stereotype.Component;

@Component
public class UniqueMemoryCache extends MemoryCache<Integer, Unique> {

}
