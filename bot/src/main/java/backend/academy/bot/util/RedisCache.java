package backend.academy.bot.util;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisCache {
    @Value("${app.cache_ttl}")
    private int cacheTTL;

    private final RedisTemplate<String, String> redisTemplate;

    public RedisCache(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(cacheTTL));
    }

    public void invalidate(String key) {
        redisTemplate.delete(key);
    }
}
