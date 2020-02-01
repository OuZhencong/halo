/**
 * Alipay.com Inc. Copyright (c) 2004-2020 All Rights Reserved.
 */
package run.halo.app.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author ouzhencong
 * @version : RedisCacheStore.java, v 0.1 2020年02月01日 11:35 上午 ouzhencong Exp $
 */
@Slf4j
public class RedisCacheStore extends StringCacheStore {

    final JedisPool pool;

    /**
     * Lock.
     */
    private Lock lock = new ReentrantLock();

    public RedisCacheStore(JedisPool pool) {
        this.pool = pool;
    }

    @Override
    Optional<CacheWrapper<String>> getInternal(String key) {
        return Optional.empty();
    }

    @Override
    void putInternal(String key, CacheWrapper<String> cacheWrapper) {

    }

    @Override
    Boolean putInternalIfAbsent(String key, CacheWrapper<String> cacheWrapper) {
        return null;
    }

    @Override
    public Optional<String> get(String key) {
        Assert.notNull(key, "Cache key must not be blank");

        Jedis jedis = null;
        try {
            jedis = pool.getResource();

            return Optional.ofNullable(jedis.get(key));
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    @Override
    public void put(String key, String value, long timeout, TimeUnit timeUnit) {
        Assert.hasText(key, "Cache key must not be blank");
        Assert.notNull(value, "Cache value must not be null");
        Assert.isTrue(timeout >= 0, "Cache expiration timeout must not be less than 1");
        Assert.notNull(timeUnit, "Cache timeUnit must not be null");

        Jedis jedis = null;
        try {
            jedis = pool.getResource();

            jedis.setex(key, (int) timeUnit.toSeconds(timeout), value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    @Override
    public Boolean putIfAbsent(String key, String value, long timeout, TimeUnit timeUnit) {
        Assert.hasText(key, "Cache key must not be blank");
        Assert.notNull(value, "Cache value must not be null");
        Assert.isTrue(timeout >= 0, "Cache expiration timeout must not be less than 1");
        Assert.notNull(timeUnit, "Cache timeUnit must not be null");

        lock.lock();
        try {
            // Get the value before
            Optional<String> valueOptional = get(key);

            if (valueOptional.isPresent()) {
                log.warn("Failed to put the cache, because the key: [{}] has been present already", key);
                return false;
            }

            // Put the cache wrapper
            put(key, value, timeout, timeUnit);
            log.debug("Put successfully");
            return true;
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void put(String key, String value) {
        Assert.hasText(key, "Cache key must not be blank");
        Assert.notNull(value, "Cache value must not be null");

        Jedis jedis = null;
        try {
            jedis = pool.getResource();

            jedis.set(key, value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    @Override
    public void delete(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();

            jedis.del(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

}