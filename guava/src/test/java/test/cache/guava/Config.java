package test.cache.guava;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class Config {
    @Bean
    public LoadingCache<Integer, Unique> loadingCacheUnique(){
        return CacheBuilder.newBuilder()
                .maximumSize(10000)
                .expireAfterAccess(60, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Unique>() {
                    @Override
                    public Unique load(Integer key) throws Exception {
                        return null;
                    }
                });
    }
}
