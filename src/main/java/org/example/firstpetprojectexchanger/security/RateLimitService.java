package org.example.firstpetprojectexchanger.security;

import org.example.firstpetprojectexchanger.exception.RateLimitExceededException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
public class RateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final boolean useRedis;

    public RateLimitService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.useRedis = true;
    }

    public void check(String bucketKey, int limitPerWindow, Duration window) {
        if (limitPerWindow <= 0) {
            return;
        }
        if (useRedis) {
            String key = "rl:" + bucketKey;
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, window);
            }
            if (count != null && count > limitPerWindow) {
                throw new RateLimitExceededException("Too many requests, please slow down");
            }
        }
    }
}
