package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 断电状态
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/10/23 15:17
 * @since v5.241002.0
 */
@Getter
@AllArgsConstructor
public enum PowerStatusEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 断电
     */
    OUTAGE("OUTAGE", "key_b058d59d584c4c13a1b0dc9c22053df9");

    private final String value;
    /**
     * 枚举信息
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

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PowerStatusEnum analyze(String value) {
        return BaseEnum.analyze(value, PowerStatusEnum.class);
    }

}

