package com.persagy.apccontrol.basic.config.cache;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.benmanes.caffeine.cache.Expiry;
import com.persagy.apccontrol.basic.model.base.CacheKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;

/**
 * 本地缓存缓存配置
 *
 * @author JiYinchuan
 * @since v4.5.0
 */
@SpringBootConfiguration
@Slf4j
public class CaffeineCacheConfigurationImpl {

    private static final String LOG_TAG = "本地缓存";

    /**
     * 注入缓存实例
     *
     * @param cacheProperties 缓存配置文件
     * @param customizers     自定义缓存
     * @param caffeine        初始化缓存
     * @param caffeineSpec    缓存规则
     * @param cacheLoader     缓存加载器
     * @return 缓存实例
     * @since v5.1.0
     */
    public static CaffeineCacheManager createInstance(
            CacheProperties cacheProperties, CacheManagerCustomizers customizers,
            ObjectProvider<Caffeine<Object, Object>> caffeine, ObjectProvider<CaffeineSpec> caffeineSpec,
            ObjectProvider<CacheLoader<Object, Object>> cacheLoader) {
        CaffeineCacheManager cacheManager = createCacheManager(cacheProperties, caffeine, caffeineSpec, cacheLoader);
        List<String> cacheNames = cacheProperties.getCacheNames();
        if (!CollectionUtils.isEmpty(cacheNames)) {
            cacheManager.setCacheNames(cacheNames);
        }
        return customizers.customize(cacheManager);
    }

    /**
     * 创建缓存实例
     *
     * @param cacheProperties 缓存配置文件
     * @param caffeine        初始化缓存
     * @param caffeineSpec    缓存规则
     * @param cacheLoader     缓存加载器
     * @return 缓存实例
     * @since v5.1.0
     */
    private static CaffeineCacheManager createCacheManager(
            CacheProperties cacheProperties,
            ObjectProvider<Caffeine<Object, Object>> caffeine, ObjectProvider<CaffeineSpec> caffeineSpec,
            ObjectProvider<CacheLoader<Object, Object>> cacheLoader) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        setCacheBuilder(cacheProperties, caffeineSpec.getIfAvailable(), caffeine.getIfAvailable(), cacheManager);
        cacheLoader.ifAvailable(cacheManager::setCacheLoader);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(16)
                .maximumSize(10000)
                .recordStats()
                .expireAfter(new Expiry<Object, Object>() {
                    @Override
                    public long expireAfterCreate(@NonNull Object key, @NonNull Object value, long currentTime) {
                        // 每次新建缓存时自动解析缓存过期时间
                        return cacheExpireTime(key);
                    }

                    @Override
                    public long expireAfterUpdate(@NonNull Object key, @NonNull Object value, long currentTime, long currentDuration) {
                        // 每次新建缓存时自动解析缓存过期时间
                        return cacheExpireTime(key);
                    }

                    @Override
                    public long expireAfterRead(@NonNull Object key, @NonNull Object value, long currentTime, long currentDuration) {
                        // 当每次读取缓存后依然使用原有的缓存剩余时间, 不进行重置
                        return currentDuration;
                    }
                }));
        return cacheManager;
    }

    /**
     * 设置缓存配置
     *
     * @param cacheProperties 缓存配置文件
     * @param caffeineSpec    缓存规则
     * @param caffeine        初始化缓存
     * @param cacheManager    缓存实例
     * @since v5.1.0
     */
    private static void setCacheBuilder(
            CacheProperties cacheProperties, CaffeineSpec caffeineSpec,
            Caffeine<Object, Object> caffeine, CaffeineCacheManager cacheManager) {
        String specification = cacheProperties.getCaffeine().getSpec();
        if (StringUtils.hasText(specification)) {
            cacheManager.setCacheSpecification(specification);
        } else if (caffeineSpec != null) {
            cacheManager.setCaffeineSpec(caffeineSpec);
        } else if (caffeine != null) {
            cacheManager.setCaffeine(caffeine);
        }
    }

    /**
     * 获取缓存过期时间
     *
     * <p>支持的单位:
     * <ul>
     *     <li>纳秒: {@code ns}</li>
     *     <li>微秒: {@code us}</li>
     *     <li>毫秒: {@code ms}</li>
     *     <li>秒: {@code s}</li>
     *     <li>分钟: {@code m}</li>
     *     <li>小时: {@code h}</li>
     *     <li>天: {@code d}</li>
     * </ul>
     *
     * @param key 缓存Key
     * @return 缓存过期时间, 单位为纳秒, 当 {@code key} 解析失败时返回 {@link CacheKey#DEFAULT_TTL} 默认缓存过期时间
     * @since v4.5.0
     */
    private static long cacheExpireTime(@NonNull Object key) {
        try {
            String keyString = key.toString();
            String time = keyString.substring(keyString.lastIndexOf("-") + 1);
            return DurationStyle.detectAndParse(time).toNanos();
        } catch (Exception e) {
            Duration defaultTtl = CacheKey.DEFAULT_TTL;
            log.warn("[{}] 解析过期时间失败, 将使用默认过期时间 [key: {}, defaultExpireTime: {}s]",
                    LOG_TAG, key, defaultTtl.getSeconds());
            return defaultTtl.toNanos();
        }
    }
}
