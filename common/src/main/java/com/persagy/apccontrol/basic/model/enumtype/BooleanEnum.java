package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 是否枚举
 *
 * @author ShaoHongbo
 * @since v5.0.0
 */
@Getter
@AllArgsConstructor
public enum BooleanEnum implements BaseEnum<Boolean>, IEnum<Boolean> {

    /**
     * 是
     */
    TRUE(true, "key_42bb8d323c484d7fa6be700b8d5f55fd"),

    /**
     * 否
     */
    FALSE(false, "key_f0173c0934b2474cb4d59ed1ee2150e3");

    /**
     * 枚举值
     */
    private final Boolean value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public Boolean value() {
        return value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static BooleanEnum analyze(Boolean value) {
        return BaseEnum.analyze(value, BooleanEnum.class);
    }
}
