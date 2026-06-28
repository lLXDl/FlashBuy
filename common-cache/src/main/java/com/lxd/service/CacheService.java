package com.lxd.service;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CacheService {
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;

    private Random random = new Random();

    public <T> T get(String key, Class<T> clazz) {
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        // 处理空对象缓存
        if (value.equals("NULL")) {
            return null;
        }
        return JSON.parseObject(value, clazz);
    }

    public void set(String key, Object value, long ttl) {
        String jsonValue = value == null ? "NULL" : JSON.toJSONString(value);
        redisTemplate.opsForValue().set(key, jsonValue, ttl, TimeUnit.SECONDS);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public long getRandomTTL(long baseTtl, long range) {
        return baseTtl + random.nextInt((int) range * 2) - range;
    }

    public boolean tryLock(String lockKey, long waitTime, long leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
