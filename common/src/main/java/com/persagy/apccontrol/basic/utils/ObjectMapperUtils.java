package com.persagy.apccontrol.basic.utils;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * Jackson序列化/反序列化工具类
 *
 * @author JiYinchuan
 * @since 7.250802.0
 */
public class ObjectMapperUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectMapperUtils.class);
    private static final String LOG_TAG = "序列化/反序列化工具栏";

    /**
     * JSON 对象反序列化为字符串键 Map 时使用的类型描述。
     *
     * @since v7.260701.0
     */
    private static final @NonNull TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE =
            new TypeReference<Map<String, Object>>() {
            };

    private static ObjectMapper objectMapper = null;

    /**
     * 转换对象
     *
     * @param object        原始对象
     * @param typeReference 目标对象类型
     * @param <T>           目标对象类型
     * @return 目标对象
     * @since 7.250902.0
     */
    public static <T> T convertValue(@Nullable Object object, @NonNull TypeReference<T> typeReference) {
        return getObjectMapper().convertValue(object, typeReference);
    }

    /**
     * 反序列化为对象
     *
     * @param object 原始对象
     * @param cls    目标对象类型
     * @param <T>    目标对象类型
     * @return 目标对象
     * @since 7.250902.0
     */
    public static <T> T convertValue(@Nullable Object object, @NonNull Class<T> cls) {
        return getObjectMapper().convertValue(object, cls);
    }

    /**
     * 反序列化为对象
     *
     * @param json          JSON字符串
     * @param typeReference 目标对象类型
     * @param <T>           目标对象类型
     * @return 目标对象
     * @since 7.250802.0
     */
    public static <T> T readValueFailSafe(@Nullable String json, @NonNull TypeReference<T> typeReference) {
        try {
            return getObjectMapper().readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            LogExecutor.project().warn(LOGGER, LOG_TAG, "反序列化失败", e, Arrays.asList(
                    Pair.of("json", json),
                    Pair.of("typeReference", typeReference)
            ));
            return null;
        }
    }

    /**
     * 将 JSON 对象反序列化为字符串键 Map。
     *
     * @param json JSON 字符串，允许为空
     * @return 字段 Map；反序列化失败时返回 {@code null}
     * @since v7.260701.0
     */
    public static @Nullable Map<String, Object> readMapFailSafe(@Nullable String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        return ObjectMapperUtils.readValueFailSafe(json, ObjectMapperUtils.MAP_TYPE_REFERENCE);
    }

    /**
     * 反序列化为对象
     *
     * @param json          JSON字符串
     * @param typeReference 目标对象类型
     * @param <T>           目标对象类型
     * @return 目标对象
     * @throws JsonProcessingException JSON处理异常
     * @since 7.250802.0
     */
    public static <T> T readValue(
            @Nullable String json,
            @NonNull TypeReference<T> typeReference)
            throws JsonProcessingException {
        return getObjectMapper().readValue(json, typeReference);
    }

    /**
     * 反序列化为对象
     *
     * @param json JSON字符串
     * @param cls  目标对象类型
     * @param <T>  目标对象类型
     * @return 目标对象
     * @since 7.250802.0
     */
    public static <T> T readValueFailSafe(@Nullable String json, @NonNull Class<T> cls) {
        try {
            return getObjectMapper().readValue(json, cls);
        } catch (JsonProcessingException e) {
            LogExecutor.project().warn(LOGGER, LOG_TAG, "反序列化失败", e, Arrays.asList(
                    Pair.of("json", json),
                    Pair.of("cls", cls)
            ));
            return null;
        }
    }

    /**
     * 反序列化为对象
     *
     * @param json JSON字符串
     * @param cls  目标对象类型
     * @param <T>  目标对象类型
     * @return 目标对象
     * @throws JsonProcessingException JSON处理异常
     * @since 7.250802.0
     */
    public static <T> T readValue(@Nullable String json, @NonNull Class<T> cls) throws JsonProcessingException {
        return getObjectMapper().readValue(json, cls);
    }

    /**
     * 对象序列化为JSON字符串
     *
     * @param object 待序列化的对象
     * @return JSON字符串
     * @since 7.250802.0
     */
    public static String writeValueAsStringFailSafe(@Nullable Object object) {
        try {
            return getObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LogExecutor.project().warn(LOGGER, LOG_TAG, "序列化失败", e, Pair.of("object", object));
            return null;
        }
    }

    /**
     * 对象序列化为JSON字符串
     *
     * @param object 待序列化的对象
     * @return JSON字符串
     * @throws JsonProcessingException JSON处理异常
     * @since 7.250802.0
     */
    public static String writeValueAsString(@Nullable Object object) throws JsonProcessingException {
        return getObjectMapper().writeValueAsString(object);
    }

    /**
     * 获取ObjectMapper实例
     *
     * @return ObjectMapper实例
     * @since 7.250802.0
     */
    public static ObjectMapper getObjectMapper() {
        if (Objects.nonNull(objectMapper)) {
            return objectMapper;
        }
        objectMapper = SpringUtil.getBean(ObjectMapper.class);
        return objectMapper;
    }
}
