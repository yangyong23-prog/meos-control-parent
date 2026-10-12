package com.persagy.apccontrol.basic.exception.enumeration;

import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;

/**
 * ThreadLocal异常枚举
 *
 * @author JiYinchuan
 * @see com.persagy.apccontrol.basic.exception.ThreadLocalException
 * @since v6.0.0
 */
public enum ThreadLocalErrorEnum implements BaseEnum<String> {

    /**
     * ThreadLocal异常
     */
    THREAD_LOCAL_ERROR("K0100", "key_9758ad817185401092d6b326adc925df"),

    /**
     * 设置的键为空
     */
    KEY_EMPTY("K0110", "key_3908941ca3e841a0b568ab911a783930"),

    /**
     * 设置的值为空
     */
    VALUE_EMPTY("K0120", "key_45fb0e647aeb4caea1dd28037b6b67e8"),

    /**
     * 获取数据结果为空
     */
    RESULT_EMPTY("K0130", "key_86d574911e664db285551585b724c95f");

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
    ThreadLocalErrorEnum(String value, String reasonPhrase) {
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
        return this.value;
    }
}
