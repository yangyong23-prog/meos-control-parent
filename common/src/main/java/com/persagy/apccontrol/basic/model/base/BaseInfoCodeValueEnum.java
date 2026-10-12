package com.persagy.apccontrol.basic.model.base;

import com.persagy.apccontrol.basic.config.annotation.InfoCode;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.exception.EnumIllegalArgumentException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.util.Map;
import java.util.Objects;

/**
 * 基础信息点枚举
 *
 * @author JiYinchuan
 * @since 7.250402.0
 */
public interface BaseInfoCodeValueEnum<V> extends BaseEnum<@NonNull V> {

    /**
     * 解析枚举参数值
     *
     * @param value           参数值
     * @param enumClass       需要解析的枚举类
     * @param <T>             参数值的类型
     * @param <E>             继承 {@link BaseInfoCodeValueEnum} 的枚举类型
     * @return 解析后的枚举对象
     * @throws EnumIllegalArgumentException 解析异常
     * @since 7.250402.0
     */
    @Nullable
    static <T, E extends BaseInfoCodeValueEnum<T>> E analyze(
            @Nullable Object value,
            @NonNull Class<E> enumClass)
            throws EnumIllegalArgumentException {
        if (Objects.isNull(value)) {
            return null;
        }
        InfoCodeEnum infoCode = getInfoCodeName(enumClass);
        if (value instanceof Map) {
            value = ((Map<?, ?>) value).get(infoCode.value());
        }
        E[] enumConstants = enumClass.getEnumConstants();
        if (Objects.nonNull(enumConstants)) {
            for (E enumConstant : enumConstants) {
                if (enumConstant.value().equals(value)) {
                    return enumConstant;
                }
            }
        }
        return null;
    }

    static <T, E extends BaseInfoCodeValueEnum<T>> @NonNull InfoCodeEnum getInfoCodeName(@NonNull Class<E> enumClass) {
        InfoCode mergedAnnotation = AnnotatedElementUtils.getMergedAnnotation(enumClass, InfoCode.class);
        if (Objects.isNull(mergedAnnotation)) {
          throw new EnumIllegalArgumentException("can not find InfoCode Annotation");
        }
        return mergedAnnotation.value();
    }
}