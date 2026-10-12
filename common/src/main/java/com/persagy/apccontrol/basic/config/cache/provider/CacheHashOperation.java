package com.persagy.apccontrol.basic.config.cache.provider;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Hash缓存操作
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
public interface CacheHashOperation {

    /**
     * 删除Hash值
     *
     * @param key      键
     * @param hashKeys Hash键
     * @return 删除数量
     * @since v5.7.0
     */
    long delete(@NonNull String key, @NonNull String... hashKeys);

    /**
     * 删除Hash值
     *
     * @param key     键
     * @param hashKey Hash键
     * @return 执行结果 [false: 失败, true: 成功]
     * @since v5.7.0
     */
    boolean hasKey(@NonNull String key, @NonNull String hashKey);

    /**
     * 获取值
     *
     * @param key     键
     * @param hashKey Hash键
     * @param <V>     值类型
     * @return 值, 当键不存在或在 {@code Pipelined/Transaction} 模式时返回 {@code null}
     * @since v5.7.0
     */
    @Nullable
    default <V> V get(@NonNull String key, @NonNull String hashKey) {
        return this.get(key, hashKey, null);
    }

    /**
     * 获取值
     *
     * @param key              键
     * @param hashKey          Hash键
     * @param defaultNullValue 当值为 {@code null} 时的默认值
     * @param <V>              值类型
     * @return 值, 当键不存在或在 {@code Pipelined/Transaction} 模式时返回 {@code null}
     * @since v5.7.0
     */
    <V> V get(@NonNull String key, @NonNull String hashKey, @Nullable Supplier<V> defaultNullValue);

    /**
     * 批量获取值
     *
     * @param key 键
     * @param hashKeys Hash键
     * @return 在 {@code Pipelined/Transaction} 模式时返回 {@code null}
     * @since 5.241002.0
     */
    <V> List<V> multiGet(@NonNull String key, @NonNull Collection<String> hashKeys);

    /**
     * 设置值
     *
     * @param key     键
     * @param hashKey Hash键
     * @param value   值
     * @since v5.7.0
     */
    void put(@NonNull String key, @NonNull String hashKey, @NonNull Object value);

    /**
     * 设置值
     *
     * @param key   键
     * @param value 值
     * @since v5.7.0
     */
    void putAll(@NonNull String key, @NonNull Map<String, Object> value);

    /**
     * 递增值
     *
     * @param key 键
     * @param hashKey Hash键
     * @param delta 地增值
     * @return 在 {@code Pipelined/Transaction} 模式时返回 {@code null}
     * @since 5.241002.0
     */
    Long increment(String key, String hashKey, long delta);

    /**
     * 递增值
     *
     * @param key 键
     * @param hashKey Hash键
     * @param delta 地增值
     * @return 在 {@code Pipelined/Transaction} 模式时返回 {@code null}
     * @since 5.241002.0
     */
    Double increment(String key, String hashKey, double delta);

    /**
     * 获取所有Hash键
     *
     * @param key 键
     * @return 所有Hash键
     * @since v5.7.0
     */
    Set<String> keys(@NonNull String key);

    /**
     * 获取Hash键大小
     *
     * @param key 键
     * @return Hash键大小
     * @since v5.7.0
     */
    long size(@NonNull String key);

}
