package com.persagy.apccontrol.basic.exception.enumeration;

import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;

/**
 * 业务异常枚举
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
public enum BusinessErrorEnum implements BaseEnum<String> {

    /**
     * 业务异常
     */
    BUSINESS_ERROR("K0000", "key_003a2a6d7a814de290f8571143432c82"),

    /**
     * 配置异常
     */
    SETTING_ERROR("K0100", "key_f81edf60d5c14b20ad5ecbbb8254b9b6"),

    /**
     * 数据异常
     */
    DATA_ERROR("K0200", "key_ab4f50a8fc6645ee9ea435773b332136");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * 构造器
     *
     * @param value        枚举值
     * @param reasonPhrase 枚举信息
     * @since v6.0.0
     */
    BusinessErrorEnum(String value, String reasonPhrase) {
        this.value = value;
        this.reasonPhrase = reasonPhrase;
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
        return String.valueOf(this.value);
    }
}
