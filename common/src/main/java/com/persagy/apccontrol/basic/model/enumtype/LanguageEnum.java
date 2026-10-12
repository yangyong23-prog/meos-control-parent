package com.persagy.apccontrol.basic.model.enumtype;

import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;

/**
 * 语言枚举
 *
 * @author JiYinchuan
 * @since 7.250901.0
 */
@AllArgsConstructor
public enum LanguageEnum implements BaseEnum<String> {

    /**
     * 中文
     */
    CHINA("zh", "China"),

    /**
     * 英文
     */
    ENGLISH("en", "English");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举描述
     */
    private final String reasonPhrase;

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public String getReasonPhrase() {
        return this.reasonPhrase;
    }
}
