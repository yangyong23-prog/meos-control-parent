package com.persagy.apccontrol.basic.config.cache.redis;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.persagy.apccontrol.basic.model.base.CommonConst.APPLICATION_NAME;

/**
 * Redis配置
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@SpringBootConfiguration
public class RedisConfiguration {

    public static final String ORIGINAL_REDIS_TEMPLATE_BEAN_NAME = "redisTemplate";
    public static final String APPLICATION_REDIS_TEMPLATE_BEAN_NAME = "applicationRedisTemplate";

    /**
     * Jackson序列化器
     */
    private static ObjectMapper objectMapper;

    /**
     * 构造器
     *
     * @param objectMapper Jackson序列化器
     * @since v5.7.0
     */
    public RedisConfiguration(ObjectMapper objectMapper) {
        RedisConfiguration.objectMapper = objectMapper;
    }

    @Primary
    @Bean(ORIGINAL_REDIS_TEMPLATE_BEAN_NAME)
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(originalKeySerializer());
        template.setHashKeySerializer(originalKeySerializer());
        template.setValueSerializer(originalValueSerializer());
        template.setHashValueSerializer(originalValueSerializer());
        return template;
    }

    @Bean(APPLICATION_REDIS_TEMPLATE_BEAN_NAME)
    public RedisTemplate<String, Object> applicationRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(applicationKeySerializer());
        template.setHashKeySerializer(originalKeySerializer());
        template.setValueSerializer(withClassValueSerializer());
        template.setHashValueSerializer(withClassValueSerializer());
        return template;
    }

    @Bean(RedisCacheTemplate.ORIGINAL_REDIS_CACHE_TEMPLATE_BEAN_NAME)
    public RedisCacheTemplate originalRedisCacheTemplate(
            @Qualifier(ORIGINAL_REDIS_TEMPLATE_BEAN_NAME)
            RedisTemplate<String, Object> redisTemplate) {
        return new RedisCacheTemplate(redisTemplate);
    }

    @Primary
    @Bean(RedisCacheTemplate.APPLICATION_REDIS_CACHE_TEMPLATE_BEAN_NAME)
    public RedisCacheTemplate redisCacheTemplate(
            @Qualifier(APPLICATION_REDIS_TEMPLATE_BEAN_NAME)
            RedisTemplate<String, Object> redisTemplate) {
        return new RedisCacheTemplate(redisTemplate);
    }

    /**
     * 原始RedisKey序列化规则
     *
     * @return RedisSerializer
     * @since v5.7.0
     */
    public static RedisSerializer<String> originalKeySerializer() {
        return RedisSerializer.string();
    }

    /**
     * RedisKey序列化规则
     *
     * @return RedisSerializer
     * @since v5.7.0
     */
    public static RedisSerializer<String> applicationKeySerializer() {
        return KeyWithApplicationSerializer.UTF_8;
    }

    /**
     * 原始RedisValue序列化规则
     *
     * @return RedisSerializer
     * @since v5.7.0
     */
    public static RedisSerializer<Object> originalValueSerializer() {
        return new GenericJackson2JsonRedisSerializer(objectMapper.copy());
    }

    /**
     * RedisValue序列化规则
     *
     * @return RedisSerializer
     * @since v5.1.0
     */
    public static RedisSerializer<Object> withClassValueSerializer() {
        return new GenericJackson2JsonRedisSerializer(objectMapper.copy()
                .activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(),
                        ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
        );
    }

    /**
     * RedisKey序列化器
     *
     * @author JiYinchuan
     * @since v5.7.0
     */
    public static class KeyWithApplicationSerializer extends StringRedisSerializer {

        public static final KeyWithApplicationSerializer UTF_8 =
                new KeyWithApplicationSerializer(StandardCharsets.UTF_8);

        /**
         * 构造器
         *
         * @param charset 字符编码
         * @since v5.7.0
         */
        public KeyWithApplicationSerializer(Charset charset) {
            super(charset);
        }

        @Override
        public String deserialize(byte[] bytes) {
            return super.deserialize(bytes);
        }

        @Override
        public byte[] serialize(String string) {
            return super.serialize(getFullKey(string));
        }

        /**
         * 获取完整Key
         *
         * @param key 原始Key
         * @return 完整Key
         * @since v5.7.0
         */
        private String getFullKey(String key) {
            if (Objects.isNull(key)) {
                return null;
            }
            String prefix = APPLICATION_NAME.toLowerCase();
            return key.startsWith(prefix) ? key : (prefix + CacheKeyPrefix.SEPARATOR + key);
        }
    }
}
