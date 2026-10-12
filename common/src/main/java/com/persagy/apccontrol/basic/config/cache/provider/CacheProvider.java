package com.persagy.apccontrol.basic.config.cache.provider;


import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 缓存提供者
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
public interface CacheProvider {

    /**
     * 查询匹配的键
     *
     * @param pattern 正则表达式
     * @return 匹配的键集合
     * @since v5.7.0
     */
    @NonNull
    Set<String> keys(@NonNull String pattern);

    /**
     * 设置缓存失效时间
     *
     * @param key        键
     * @param expireTime 过期时间
     * @param timeUnit   过期时间单位
     * @return 执行结果 [false: 失败, true: 成功]
     * @since v5.7.0
     */
    boolean expire(@NonNull String key, long expireTime, @NonNull TimeUnit timeUnit);

    /**
     * 设置缓存失效时间
     *
     * @param key        键
     * @param expireTime 过期时间
     * @return 执行结果 [false: 失败, true: 成功]
     * @since v5.7.0
     */
    boolean expire(@NonNull String key, @NonNull Duration expireTime);

    /**
     * 设置缓存失效时间
     *
     * @param key          键
     * @param expireSecond 过期时间, 单位 [秒]
     * @return 执行结果 [false: 失败, true: 成功]
     * @since v5.7.0
     */
    default boolean expire(@NonNull String key, long expireSecond) {
        return expire(key, expireSecond, TimeUnit.SECONDS);
    }

    /**
     * 查询键过期时间
     *
     * @param key 键
     * @return 过期时间, 单位 [秒], 不存在则小于 [0]
     * @since v5.7.0
     */
    default long getExpire(@NonNull String key) {
        return this.getExpire(key, null);
    }

    /**
     * 查询键过期时间
     *
     * @param key              键
     * @param defaultNonExpire 当过期时间不存在时的默认过期时间
     * @return 过期时间, 单位 [秒], 不存在则小于 [0]
     * @since v5.7.0
     */
    long getExpire(@NonNull String key, @Nullable Supplier<Long> defaultNonExpire);

    /**
     * 判断键是否存在
     *
     * @param key 键
     * @return 是否存在 [true: 存在, false: 不存在]
     * @since v5.7.0
     */
    boolean hasKey(@NonNull String key);

    /**
     * 删除键
     *
     * @param key 键
     * @return 被删除的键数量 [&lt;0: 操作失败, =0: 未执行操作, &gt;0: 删除成功的键数量]
     * @since v5.7.0
     */
    long del(@NonNull String... key);
    
    /**
     * 模糊删除全量 KEY
     * 匹配策略 tes% 所有这个 key开头的缓存都删除
     *
     * @param keyPattern 键
     * @return 被删除的键数量 [&lt;0: 操作失败, =0: 未执行操作, &gt;0: 删除成功的键数量]
     */
    Long fuzzyMatchingDel(@NonNull String keyPattern);
    
    /**
     * 数据更新删除缓存
     */
    void dataUpdatingDelCache();

}
