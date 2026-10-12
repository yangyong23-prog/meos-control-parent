package com.persagy.apccontrol.basic.model.enumtype.security;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通行需求状态
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/5 10:05
 * @since v5.240801.0
 */
@Getter
@AllArgsConstructor
public enum AccessDemandStatusEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 常开
     */
    NORMALLY_OPEN("NORMALLY_OPEN", "key_b7cab2256c1b42ec8d9330da432409a7", "1"),
    /**
     * 常闭
     */
    NORMAL_CLOSE("NORMAL_CLOSE", "key_38c45bb3812f4ef79f81211096907985", "2"),
    /**
     * 有权限通过
     */
    PERMISSION_PASS("PERMISSION_PASS", "key_62dbb8522c9649539a5d9b87fe192cd4", "3"),
    /**
     * 无法监测, 空间通行状态不可获取
     */
    UNMONITORED("UNMONITORED", "key_451b530e2875488e8d6f376716205883", "4");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * 条件值
     */
    private final String conditionValue;


    @Override
    public String value() {
        return this.value;
    }

    public String conditionValue() {
        return this.conditionValue;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(this.reasonPhrase);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AccessDemandStatusEnum analyze(String value) {
        return BaseEnum.analyze(value, AccessDemandStatusEnum.class);
    }

}


