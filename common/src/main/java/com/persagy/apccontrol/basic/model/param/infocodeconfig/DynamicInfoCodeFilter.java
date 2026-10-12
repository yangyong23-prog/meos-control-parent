package com.persagy.apccontrol.basic.model.param.infocodeconfig;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

/**
 * @ClassName 薛涛
 * @Author 薛涛
 * @Date 2024/7/3 14:46
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DynamicInfoCodeFilter {

    /**
     * 信息点编码
     */
    private String infoCode;

    /**
     * 信息点值
     */
    private Object dataValue;

    /**
     * 比较信息点值
     *
     * @param infoCodeValue 信息点值
     * @return 是否相等
     * @since 7.250601.0
     */
    @JsonIgnore
    public boolean equalsDataValue(@Nullable InfoCodeValueVO infoCodeValue) {
        return this.equalsDataValue(Objects.nonNull(infoCodeValue) ? infoCodeValue.getInfoCodeValue() : null);
    }

    /**
     * 比较信息点值
     *
     * @param infoCodeValue 信息点值
     * @return 是否相等
     * @since 7.250601.0
     */
    @JsonIgnore
    public boolean equalsDataValue(@Nullable Object infoCodeValue) {
        // 信息点值同时为 null 时则匹配
        if (Objects.isNull(this.dataValue) && Objects.isNull(infoCodeValue)) {
            return true;
        }
        // 信息点值一个为 null 另一个不为 null 时则不匹配
        if (ObjectUtils.anyNull(this.dataValue, infoCodeValue)) {
            return false;
        }
        return this.equalsDataValue(this.dataValue, infoCodeValue);
    }

    /**
     * 比较数据值
     *
     * @param source 数据源
     * @param target 目标源
     * @return 是否相等
     * @since 7.251002.0
     */
    private boolean equalsDataValue(@NonNull Object source, @NonNull Object target) {
        if (source instanceof Collection) {
            // 两个均为集合时判断是否存在交集
            if (target instanceof Collection) {
                return CollectionUtils.isNotEmpty(CollectionUtils.intersection(
                        (Collection<?>) source, (Collection<?>) target));
            }
            // 判断 source 是否包含 target
            return ((Collection<?>) source).contains(target);
        } else {
            // 判断 target 是否包含 source
            if (target instanceof Collection) {
                return ((Collection<?>) target).contains(source);
            }
            // 判断 target 和 source 是否相等
            return Objects.equals(source, target);
        }
    }
}
