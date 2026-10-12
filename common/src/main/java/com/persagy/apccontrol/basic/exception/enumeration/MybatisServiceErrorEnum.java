package com.persagy.apccontrol.basic.exception.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.exception.ApplicationException;

/**
 * MybatisPlus异常枚举
 *
 * @author JiYinchuan
 * @see ApplicationException
 * @since v6.0.0
 */
public enum MybatisServiceErrorEnum implements BaseEnum<String> {

    /**
     * MybatisService异常
     */
    MYBATIS_SERVICE_ERROR("M0100", "key_3c562a9195604791a34120e7d61f6de5"),

    /**
     * ID为空
     */
    ID_IS_NULL("M0101", "key_e011510807074dd183e554e4200ff410"),

    /**
     * 结果为空
     */
    RESULT_IS_NULL("M0102", "key_ffcfca102d4240b6b9b890c36d86679a"),

    /**
     * 结果集为空
     */
    RESULT_SET_IS_NULL("M0103", "key_194a752f414849d0933490514bc3e243"),

    /**
     * 执行操作异常
     */
    OPERATION_ERROR("M0110", "key_73e329796319490aa26fa4d94c5c5b6c"),

    /**
     * ID异常
     */
    ID_ERROR("M0111", "key_178fb3d6be8b4939bc991c0376fd0801"),

    /**
     * 幂等性重复
     */
    IDEMPOTENT_REPETITION("M0120", "key_deba4d2afa81471eae96f95da32b8a6c");

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
    MybatisServiceErrorEnum(String value, String reasonPhrase) {
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

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static MybatisServiceErrorEnum analyze(String value) {
        return BaseEnum.analyze(value, MybatisServiceErrorEnum.class);
    }
}
