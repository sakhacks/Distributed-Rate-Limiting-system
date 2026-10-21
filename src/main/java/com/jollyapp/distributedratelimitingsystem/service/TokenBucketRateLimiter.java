package com.jollyapp.distributedratelimitingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class TokenBucketRateLimiter {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<Long> tokenBucketScript;

    @Autowired
    public TokenBucketRateLimiter(RedisTemplate<String, String> redisTemplate,
                                  RedisScript<Long> tokenBucketScript) {
        this.redisTemplate = redisTemplate;
        this.tokenBucketScript = tokenBucketScript;
    }

    public boolean isAllowed(String apiKey, int capacity, double refillRate) {
        String key = "ratelimit:token_bucket:" + apiKey;
        long now = System.currentTimeMillis();

        Long result = redisTemplate.execute(
                tokenBucketScript,
                Collections.singletonList(key),
                String.valueOf(capacity),
                String.valueOf(refillRate),
                String.valueOf(now)
        );

        return result != null && result == 1L;
    }
}
