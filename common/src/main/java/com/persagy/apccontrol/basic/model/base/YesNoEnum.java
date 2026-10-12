package com.persagy.apccontrol.basic.model.base;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;

/**
 * 是否枚举
 *
 * @author LiuYang
 * @since 7.250102.0
 */
@AllArgsConstructor
public enum YesNoEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 是
     */
    YES("YES", "key_42bb8d323c484d7fa6be700b8d5f55fd"),

    /**
     * 否
     */
    NO("NO", "key_f0173c0934b2474cb4d59ed1ee2150e3");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(this.reasonPhrase);
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static YesNoEnum resolve(String value) {
        return BaseEnum.analyze(value, YesNoEnum.class, false);
    }
}
