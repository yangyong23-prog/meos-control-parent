package com.persagy.apccontrol.basic.outside.enums.abnormalevent;

import com.alibaba.fastjson.serializer.JSONSerializable;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.Getter;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 一级风控目标
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/7/19 14:34
 * @since v5.240502.0
 */
@Getter
public enum RiskMeasureAimLevel1 implements JSONSerializable, BaseEnum<String> {
    E0("E0", 1, "key_24fcd0bfebc147a1a3fd7a6bf18179c9", false, ""),
    E1("E1", 2, "key_c848d1ebabd54e3aa3f5f99556c395c1", false, ""),
    E2("E2", 3, "key_e506e7deaca0493dbbf93bb72e1c7be7", false, ""),
    E3("E3", 4, "key_2a7926bac10e475ca67e3b739ffa2083", false, ""),
    ;

    private final String code;
    @EnumValue
    private final int value;
    private final String desc;
    private final boolean isDelete;
    private final String extraValue;

    public void write(JSONSerializer serializer, Object fieldName, Type fieldType, int features) throws IOException {
        serializer.write(this.code);
    }

    public String getCode() {
        return this.code;
    }

    public int getValue() {
        return this.value;
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(this.desc);
    }

    public boolean isDelete() {
        return this.isDelete;
    }

    private RiskMeasureAimLevel1(final String code, final int value, final String desc, final boolean isDelete, final String extraValue) {
        this.code = code;
        this.value = value;
        this.desc = desc;
        this.isDelete = isDelete;
        this.extraValue = extraValue;
    }

    @Override
    public String value() {
        return code;
    }

    @Override
    public String getReasonPhrase() {
        return desc;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RiskMeasureAimLevel1 analyze(String value) {
        return BaseEnum.analyze(value, RiskMeasureAimLevel1.class);
    }
}