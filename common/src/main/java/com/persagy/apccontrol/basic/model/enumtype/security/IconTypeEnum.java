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
public enum IconTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 一类小图标
     */
    FIRST_CLASS_SMALL_ICON("FIRST_CLASS_SMALL_ICON", "key_957cb6537db547789bd40c1f307bef33"),
    /**
     * 二类小图标
     */
    SECOND_CLASS_SMALL_ICON("SECOND_CLASS_SMALL_ICON", "key_c23174dad0fa498fa7881542108b4901"),
    /**
     * 普通图标
     */
    NORMAL_ICON("NORMAL_ICON", "key_2f056976484547019b491996bce43791");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;


    @Override
    public String value() {
        return this.value;
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
    public static IconTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, IconTypeEnum.class);
    }

    public static IconTypeEnum objectToEnum(Object value) {
        return analyze(String.valueOf(value));
    }

}


