package com.persagy.apccontrol.basic.structure.demand.model;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.structure.common.parser.PropertyDescriptorParser;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.NonNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基础需求配置值对象
 *
 * @author JiYinchuan
 * @since 5.240701.0
 */
@Data
public abstract class BaseDemandConfig implements PropertyDescriptorParser, BaseCheckParamProvider {

    private static ObjectMapper objectMapper;


    protected BaseDemandConfig() {
        // 可以在此处进行一些基本的初始化
    }

    /**
     * 填充默认值
     *
     * @param instanceType 实例类型
     * @since 5.240902.0
     */
    public void fillDefaultConfig(@NonNull DemandInstanceTypeEnum instanceType) {

    }

    /**
     * 是否所有字段为空
     *
     * @return 是否任意一个字段不为空
     * @since 5.241101.0
     */
    public boolean allFieldEmpty() {
        return !anyFieldNotEmpty();
    }

    /**
     * 是否任意一个字段不为空
     *
     * @return 是否任意一个字段不为空
     * @since 5.241002.0
     */
    @SneakyThrows
    public boolean anyFieldNotEmpty() {
        for (String fieldName : cacheFieldNames()) {
            Object fieldValue = cachePropertyDescriptor(fieldName).getReadMethod().invoke(this);
            if (Objects.nonNull(fieldValue)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否所有字段都不为空
     *
     * @return 是否所有字段都不为空
     * @since 5.241002.0
     */
    @SneakyThrows
    public boolean allFieldNotEmpty() {
        for (String fieldName : cacheFieldNames()) {
            Object fieldValue = cachePropertyDescriptor(fieldName).getReadMethod().invoke(this);
            if (Objects.isNull(fieldValue)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 从Map冲读取值
     *
     * @param source 数据源
     * @param tClass 值类型
     * @param <T>    值类型
     * @return 值
     * @since 5.241002.0
     */
    protected <T> List<T> readValueFromMap(@NonNull Collection<Map<String, Object>> source, @NonNull Class<T> tClass) {
        return CollectionUtils.isNotEmpty(source)
                ? source.stream().map(item -> readValueFromMap(item, tClass)).collect(Collectors.toList())
                : Lists.newArrayList();
    }

    /**
     * 从Map冲读取值
     *
     * @param source 数据源
     * @param tClass 值类型
     * @param <T>    值类型
     * @return 值
     * @since 5.241002.0
     */
    protected <T> T readValueFromMap(@NonNull Map<String, Object> source, @NonNull Class<T> tClass) {
        try {
            return getObjectMapper().readValue(getObjectMapper().writeValueAsString(source), tClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取ObjectMapper实例
     *
     * @return ObjectMapper实例
     * @since 5.241002.0
     */
    private static ObjectMapper getObjectMapper() {
        return Optional.ofNullable(objectMapper).orElseGet(() -> SpringUtil.getBean(ObjectMapper.class));
    }
}
