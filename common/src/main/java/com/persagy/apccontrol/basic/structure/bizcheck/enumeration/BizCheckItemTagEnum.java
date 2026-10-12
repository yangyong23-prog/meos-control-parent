package com.persagy.apccontrol.basic.structure.bizcheck.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 * @description:
 * @author: wxf
 * @create: 2026-02-27 18:12
 **/
@Getter
@AllArgsConstructor
public enum BizCheckItemTagEnum implements BaseEnum<String>, IEnum<String> {
    /**
     *今天
     */
    TODAY("TODAY", "key_check_type_today"),

    /**
     *明天
     */
    TOMORROW("TOMORROW", "key_check_type_tomorrow");

    /**
     *枚值举值
     */
    private final String value;

    /**
     *枚信息信息
     */
    private final String reasonPhrase;

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @Override
    public String toString() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static BizCheckTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, BizCheckTypeEnum.class);
    }
}
