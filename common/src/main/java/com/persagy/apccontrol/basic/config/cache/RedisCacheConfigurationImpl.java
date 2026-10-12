package com.persagy.apccontrol.basic.config.cache;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.config.cache.redis.RedisConfiguration;
import com.persagy.apccontrol.basic.model.base.CacheDynamicKey;
import com.persagy.apccontrol.basic.model.base.CacheKey;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.framework.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.lang.Nullable;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.*;

/**
 * 缓存配置
 *
 * @author JiYinchuan
 * @see CacheKey
 * @since v5.1.0
 */
@SpringBootConfiguration
@Slf4j
public class RedisCacheConfigurationImpl {

    private static final String LOG_TAG = "缓存配置";

    /**
     * 应用名称
     */
    private static final String APPLICATION_NAME = CommonConst.APPLICATION_NAME;

    @Autowired
    private CacheProperties cacheProperties;

    /**
     * 注入缓存示例
     *
     * @param cacheProperties                     缓存配置文件
     * @param cacheManagerCustomizers             自定义缓存
     * @param redisCacheConfiguration             Redis缓存配置
     * @param redisCacheManagerBuilderCustomizers Redis自定义缓存
     * @param redisConnectionFactory              Redis连接工厂
     * @return 缓存实例
     * @since v5.1.0
     */
    public static RedisCacheManager createInstance(
            CacheProperties cacheProperties, CacheManagerCustomizers cacheManagerCustomizers,
            RedisCacheConfiguration redisCacheConfiguration,
            ObjectProvider<RedisCacheManagerBuilderCustomizer> redisCacheManagerBuilderCustomizers,
            RedisConnectionFactory redisConnectionFactory) {
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration);
        List<String> cacheNames = cacheProperties.getCacheNames();
        if (!cacheNames.isEmpty()) {
            builder.initialCacheNames(new LinkedHashSet<>(cacheNames));
        }
        if (cacheProperties.getRedis().isEnableStatistics()) {
            builder.enableStatistics();
        }
        redisCacheManagerBuilderCustomizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
        return cacheManagerCustomizers.customize(builder.build());
    }

    /**
     * RedisCacheManagerBuilderCustomizer
     *
     * @return RedisCacheManagerBuilderCustomizer
     * @since v5.1.0
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return builder -> {
            Map<String, RedisCacheConfiguration> initialCacheConfigurations = new HashMap<>(16);
            Field[] fields = CacheDynamicKey.class.getFields();
            if (ArrayUtils.isNotEmpty(fields)) {
                for (Field field : fields) {
                    String fieldValue;
                    try {
                        fieldValue = (String) field.get(null);
                    } catch (IllegalAccessException e) {
                        throw new BusinessException(ResponseCode.SYSTEM_ERROR_B0001.getCode(), LocaleMessageUtil.getMessage("key_70e1547a4823445eb595a5374106d5d2"), e);
                    }
                    if (fieldValue.startsWith(CacheKey.Dynamic.DYNAMIC_KEY_PREFIX)) {
                        if (field.getType() != String.class) {
                            throw new BusinessException(ResponseCode.SYSTEM_ERROR_B0001.getCode(), LocaleMessageUtil.getMessage("key_ffea07c026fa44e0ac6c66323f084c57"));
                        }
                        Duration timeDuration = null;
                        String timeString = fieldValue.substring(
                                fieldValue.lastIndexOf(CacheKey.Dynamic.DYNAMIC_KEY_TIMEOUT_SPLIT) + 1);
                        if (StringUtils.isNotBlank(timeString)) {
                            try {
                                timeDuration = DurationStyle.detectAndParse(timeString);
                            } catch (IllegalArgumentException e) {
                                log.warn("[{}] 解析过期时间失败, 将使用默认过期时间 [cacheName: {}, defaultExpireTime: {}s]",
                                        LOG_TAG, fieldValue, CacheKey.DEFAULT_TTL.getSeconds());
                            }
                            if (Objects.nonNull(timeDuration) && timeDuration.compareTo(Duration.ofSeconds(1)) < 0) {
                                throw new BusinessException(ResponseCode.SYSTEM_ERROR_B0001.getCode(), LocaleMessageUtil.getMessage("key_77635e4270064ffba14e5c20d488c877"));
                            }
                        }
                        initialCacheConfigurations.put(fieldValue, createRedisCacheConfiguration(timeDuration));
                    }
                }
            }
            builder.withInitialCacheConfigurations(initialCacheConfigurations).transactionAware();
        };
    }

    /**
     * RedisCacheConfiguration
     *
     * @return RedisCacheConfiguration
     * @since v5.1.0
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        return createRedisCacheConfiguration(cacheProperties.getRedis().getTimeToLive());
    }

    /**
     * 创建Redis缓存配置
     *
     * @param ttl 缓存过期时间
     * @return Redis缓存配置
     * @since v5.1.0
     */
    private RedisCacheConfiguration createRedisCacheConfiguration(@Nullable Duration ttl) {
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        Duration entryTtl = ObjectUtils.defaultIfNull(ttl, CacheKey.DEFAULT_TTL);
        String prefixCacheNameWith = StringUtils.defaultString(
                redisProperties.getKeyPrefix(), APPLICATION_NAME.toLowerCase());
        RedisSerializationContext.SerializationPair<String> serializeKeysWith =
                RedisSerializationContext.SerializationPair.fromSerializer(RedisConfiguration.originalKeySerializer());
        RedisSerializationContext.SerializationPair<Object> serializeValuesWith =
                RedisSerializationContext.SerializationPair.fromSerializer(RedisConfiguration.withClassValueSerializer());
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(entryTtl)
                .prefixCacheNameWith(prefixCacheNameWith + CacheKeyPrefix.SEPARATOR)
                .serializeKeysWith(serializeKeysWith)
                .serializeValuesWith(serializeValuesWith);
        if (!redisProperties.isCacheNullValues()) {
            redisCacheConfiguration = redisCacheConfiguration.disableCachingNullValues();
        }
        if (!redisProperties.isUseKeyPrefix()) {
            redisCacheConfiguration = redisCacheConfiguration.disableKeyPrefix();
        }
        return redisCacheConfiguration;
    }
}
