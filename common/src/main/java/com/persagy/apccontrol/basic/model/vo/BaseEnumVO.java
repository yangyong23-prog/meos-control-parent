package com.persagy.apccontrol.basic.model.vo;

import com.persagy.validate.utility.enumeration.base.BaseEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * 基础枚举VO
 *
 * @author JiYinchuan
 * @since v5.8.0
 */
@Data
@Schema(title = "基础枚举VO")
public class BaseEnumVO implements Comparable<BaseEnumVO> {

    /**
     * Key
     */
    private Object key;

    /**
     * Value
     */
    private Object value;

    /**
     * 用户提示语
     */
    private Object userTips;

    /**
     * 拓展值
     */
    private String additionalValue;

    /**
     * 拓展值
     */
    private Map<String, String> additionalMapValue;

    /**
     * 构造器
     *
     * @since v5.8.0
     */
    public BaseEnumVO() {
    }

    /**
     * 构造器
     *
     * @param key   Key
     * @param value Value
     * @since v5.8.0
     */
    public BaseEnumVO(Object key, Object value) {
        this.key = key;
        this.value = value;
    }

    /**
     * 构造器
     *
     * @param key             Key
     * @param value           Value
     * @param additionalValue 拓展值
     * @since 5.240502.0
     */
    public BaseEnumVO(Object key, Object value, String additionalValue) {
        this.key = key;
        this.value = value;
        this.additionalValue = additionalValue;
    }

    /**
     * 构造器
     *
     * @param key                Key
     * @param value              Value
     * @param additionalMapValue 拓展值
     * @since v5.8.0
     */
    public BaseEnumVO(Object key, Object value, Map<String, String> additionalMapValue) {
        this.key = key;
        this.value = value;
        this.additionalMapValue = additionalMapValue;
    }

    /**
     * 构造器
     *
     * @param key                Key
     * @param value              Value
     * @param additionalValue    拓展值
     * @param additionalMapValue 拓展值
     * @since 5.240502.0
     */
    public BaseEnumVO(Object key, Object value, String additionalValue, Map<String, String> additionalMapValue) {
        this.key = key;
        this.value = value;
        this.additionalValue = additionalValue;
        this.additionalMapValue = additionalMapValue;
    }

    /**
     * 构造器
     *
     * @param baseEnum 基础枚举
     * @since 5.240402.0
     */
    public <T extends BaseEnum<?>> BaseEnumVO(T baseEnum) {
        this.key = baseEnum.value();
        this.value = baseEnum.getReasonPhrase();
        this.additionalValue = baseEnum.additionalValue();
        this.additionalMapValue = baseEnum.additionalMapValue();
    }

    /**
     * 获取用户提示语
     *
     * @return 用户提示语
     * @since 5.240502.0
     */
    public Object getUserTips() {
        return ObjectUtils.defaultIfNull(userTips, value);
    }

    /**
     * 添加拓展值
     *
     * @param key   拓展值Key
     * @param value 拓展值Value
     * @return 当前实例
     * @since 5.240502.0
     */
    public BaseEnumVO addAdditionalMapValue(String key, String value) {
        this.additionalMapValue.put(key, value);
        return this;
    }

    @Override
    public int compareTo(@NonNull BaseEnumVO o) {
        Object sourceKey = this.getKey();
        Object targetKey = o.getKey();
        if (Objects.isNull(sourceKey) || Objects.isNull(targetKey)) {
            return 0;
        }
        if (sourceKey instanceof String && targetKey instanceof String) {
            String sourcekeyString = (String) sourceKey;
            String targetkeyString = (String) targetKey;
            if (NumberUtils.isParsable(sourcekeyString) && NumberUtils.isParsable(targetkeyString)) {
                Number sourceKeyNumber = NumberUtils.createNumber(sourcekeyString);
                Number targetKeyNumber = NumberUtils.createNumber(targetkeyString);
                return BigDecimal.valueOf(sourceKeyNumber.doubleValue()).compareTo(
                        BigDecimal.valueOf(targetKeyNumber.doubleValue()));
            }
            return ObjectUtils.compare(sourcekeyString, targetkeyString);
        }
        if (sourceKey instanceof Integer && targetKey instanceof Integer) {
            return ObjectUtils.compare((Integer) sourceKey, (Integer) targetKey);
        }
        return 0;
    }
}
