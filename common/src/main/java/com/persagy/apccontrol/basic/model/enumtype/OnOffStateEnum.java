package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 开关状态
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/5/24 14:17
 * @since v5.240502.0
 */
@Getter
@AllArgsConstructor
public enum OnOffStateEnum implements BaseEnum<String>, IEnum<String> {
    /**
     * 开启
     */
    OPEN("OPEN", "key_5f6a4957123248279ddf22f6cfa74063"),

    /**
     * 关闭
     */
    CLOSE("CLOSE", "key_b1620633958840638440dd1b3e10024e");

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
    public static OnOffStateEnum analyze(String value) {
        return BaseEnum.analyze(value, OnOffStateEnum.class);
    }
}
