package com.persagy.apccontrol.basic.structure.common.parser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.utils.cls.PropertyDescriptorUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 属性描述符解析器
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
public interface PropertyDescriptorParser {

    /**
     * 缓存控制参数字段名称
     *
     * @return 缓存控制参数字段名称
     * @since v5.6.0
     */
    @JsonIgnore
    default List<String> cacheFieldNames() {
        return cachePropertyDescriptors().stream().map(FeatureDescriptor::getName).collect(Collectors.toList());
    }

    /**
     * 对象类下所有对象属性访问器
     *
     * @return 所有对象属性访问器
     * @since 5.241002.0
     */
    @JsonIgnore
    default List<PropertyDescriptor> cachePropertyDescriptors() {
        return PropertyDescriptorUtils.getPropertyDescriptors(this.getClass());
    }

    /**
     * 获取对象属性访问器
     *
     * @param fieldName 属性名称
     * @return 对象属性访问器
     * @since 5.240902.0
     */
    @JsonIgnore
    default @Nullable PropertyDescriptor cachePropertyDescriptor(@NonNull String fieldName) {
        return PropertyDescriptorUtils.getPropertyDescriptor(this, fieldName);
    }

    /**
     * 读取对象字段值
     *
     * @param fieldName 读取对象字段
     * @return 字段值
     * @since 7.250201.0
     */
    @JsonIgnore
    default @Nullable Object readPropertyValue(@NonNull String fieldName) {
        PropertyDescriptor descriptor = this.cachePropertyDescriptor(fieldName);
        if (Objects.isNull(descriptor)) {
            return null;
        }
        Method readMethod = descriptor.getReadMethod();
        if (Objects.isNull(readMethod)) {
            return null;
        }
        try {
            return readMethod.invoke(this);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LogExecutor.project().warn(LoggerFactory.getLogger(getClass()), "属性描述符解析器",
                    "调用读取方法异常", e, Arrays.asList(
                            Pair.of("object", this),
                            Pair.of("fieldName", fieldName)
                    ));
        }
        return null;
    }

    /**
     * 写入对象字段值
     *
     * @param fieldName  写入对象字段
     * @param fieldValue 写入对象字段值
     * @return 字段值
     * @since 7.250201.0
     */
    @JsonIgnore
    default @Nullable Object writePropertyValue(@NonNull String fieldName, @Nullable Object fieldValue) {
        PropertyDescriptor descriptor = this.cachePropertyDescriptor(fieldName);
        if (Objects.isNull(descriptor)) {
            return null;
        }
        Method writeMethod = descriptor.getWriteMethod();
        if (Objects.isNull(writeMethod)) {
            return null;
        }
        try {
            return writeMethod.invoke(this, fieldValue);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LogExecutor.project().warn(LoggerFactory.getLogger(getClass()), "属性描述符解析器",
                    "调用写入方法异常", e, Arrays.asList(
                            Pair.of("object", this),
                            Pair.of("fieldName", fieldName)
                    ));
        }
        return null;
    }

    /**
     * 字段是否为空
     *
     * @param fieldNames 字段名称, 为空时校验所有字段
     * @return 字段是否为空
     * @since 7.250801.0
     */
    @JsonIgnore
    default boolean fieldEmpty(@Nullable String... fieldNames) {
        List<String> finalFieldNames = Objects.nonNull(fieldNames) && fieldNames.length > 0
                ? Arrays.stream(fieldNames).collect(Collectors.toList())
                : cacheFieldNames();
        if (CollectionUtils.isEmpty(finalFieldNames)) {
            return false;
        }
        boolean empty = true;
        for (String fieldName : finalFieldNames) {
            Object value = readPropertyValue(fieldName);
            if (ObjectUtils.isNotEmpty(value)) {
                empty = false;
                break;
            }
        }
        return empty;
    }
}