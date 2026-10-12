package com.persagy.apccontrol.basic.config.cache.provider;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 对象缓存操作
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
public interface CacheObjectOperation {

    /**
     * 获取值
     *
     * @param key 键
     * @param <V> 值类型
     * @return 值, 当键不存在或在 {@code Pipelined/Transaction} 模式时返回 {@code null}
     * @since v5.7.0
     */
    @Nullable
    default <V> V get(@NonNull String key) {
        return this.get(key, null);
    }

    /**
     * 获取值
     *
     * @param key              键
     * @param defaultNullValue 当值为 {@code null} 时的默认值
     * @param <V>              值类型
     * @return 值, 当键不存在或在 {@code Pipelined/Transaction} 模式时返回 {@code null}
     * @since v5.7.0
     */
    <V> V get(@NonNull String key, @Nullable Supplier<V> defaultNullValue);

    /**
     * 设置值
     *
     * @param key   键
     * @param value 值
     * @since v5.7.0
     */
    void set(@NonNull String key, @NonNull Object value);

    /**
     * 设置值并指定过期时间
     *
     * @param key        键
     * @param value      值
     * @param expireTime 过期时间, 小于等于 [0] 将永不过期
     * @param timeUnit   时间单位
     * @since v5.7.0
     */
    void set(@NonNull String key, @NonNull Object value, long expireTime, @NonNull TimeUnit timeUnit);

    /**
     * 设置值并指定过期时间
     *
     * @param key        键
     * @param value      值
     * @param expireTime 过期时间
     * @since v5.7.0
     */
    void set(@NonNull String key, @NonNull Object value, @NonNull Duration expireTime);

    /**
     * 设置值并指定过期时间
     *
     * @param key        键
     * @param value      值
     * @param expireTime 过期时间, 单位 [秒], 小于等于 [0] 将永不过期
     * @since v5.7.0
     */
    default void set(@NonNull String key, @NonNull Object value, long expireTime) {
        this.set(key, value, expireTime, TimeUnit.SECONDS);
    }

    /**
     * 指定键的值进行递增
     *
     * @param key    键
     * @param factor 递增因子, 必须大于 [0]
     * @return 递增后的值, 不存在则小于 [0], 在 {@code Pipelined/Transaction} 模式时返回 {@code null}
     * @see RedisConnection#isPipelined()
     * @see RedisConnection#isQueueing()
     * @since v5.7.0
     */
    @Nullable
    Long increment(@NonNull String key, long factor);

    /**
     * 指定键的值进行递减
     *
     * @param key    键
     * @param factor 递减因子, 必须大于 [0]
     * @return 递减后的值, 不存在则小于 [0], 在 {@code Pipelined/Transaction} 模式时返回 {@code null}
     * @see RedisConnection#isPipelined()
     * @see RedisConnection#isQueueing()
     * @since v5.7.0
     */
    @Nullable
    Long decrement(@NonNull String key, long factor);

}
