package com.openrank.openrank.config.cache;

import org.apache.ibatis.cache.Cache;
import org.springframework.data.redis.core.RedisTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * MyBatis 二级缓存的 Redis 实现。
 * MyBatis 通过反射创建实例，因此使用静态方式注入 RedisTemplate。
 */
public class MybatisRedisCache implements Cache {

    private static final String CACHE_PREFIX = "mybatis:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);
    private static final Logger log = LoggerFactory.getLogger(MybatisRedisCache.class);
    private static RedisTemplate<String, Object> redisTemplate;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final String id;

    public MybatisRedisCache(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void putObject(Object key, Object value) {
        if (key == null || value == null || redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(buildKey(key), value, DEFAULT_TTL);
        } catch (Exception ex) {
            log.debug("Redis put 缓存失败，降级直连数据库: {}", ex.getMessage());
        }
    }

    @Override
    public Object getObject(Object key) {
        if (key == null || redisTemplate == null) {
            return null;
        }
        try {
            return redisTemplate.opsForValue().get(buildKey(key));
        } catch (Exception ex) {
            log.debug("Redis get 缓存失败，降级直连数据库: {}", ex.getMessage());
            return null;
        }
    }

    @Override
    public Object removeObject(Object key) {
        if (key == null || redisTemplate == null) {
            return null;
        }
        try {
            redisTemplate.delete(buildKey(key));
        } catch (Exception ex) {
            log.debug("Redis remove 缓存失败，忽略: {}", ex.getMessage());
        }
        return null;
    }

    @Override
    public void clear() {
        if (redisTemplate == null) {
            return;
        }
        try {
            Set<String> keys = redisTemplate.keys(buildKey("*"));
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception ex) {
            log.debug("Redis clear 缓存失败，忽略: {}", ex.getMessage());
        }
    }

    @Override
    public int getSize() {
        if (redisTemplate == null) {
            return 0;
        }
        try {
            Set<String> keys = redisTemplate.keys(buildKey("*"));
            return keys == null ? 0 : keys.size();
        } catch (Exception ex) {
            log.debug("Redis size 查询失败，返回0: {}", ex.getMessage());
            return 0;
        }
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    private String buildKey(Object key) {
        return CACHE_PREFIX + id + ":" + Objects.toString(key);
    }

    public static void setRedisTemplate(RedisTemplate<String, Object> template) {
        MybatisRedisCache.redisTemplate = template;
    }
}
