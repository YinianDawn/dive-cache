package test.cache.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class Config {
    @Bean
    public RedisTemplate<String, Unique> getRedisTemplateUnique(RedisConnectionFactory factory){
        RedisTemplate<String, Unique> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        return template;
    }
}
