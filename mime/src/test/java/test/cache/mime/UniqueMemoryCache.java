package test.cache.mime;

import mime.cache.mime.MemoryCache;
import org.springframework.stereotype.Component;

@Component
public class UniqueMemoryCache extends MemoryCache<Integer, Unique> {

}
