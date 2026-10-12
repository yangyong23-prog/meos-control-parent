package com.persagy.apccontrol.basic.model.enumtype.dtprwd;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/5/2 14:09
 */
@Getter
@AllArgsConstructor
public enum DtpRwdTagEnum implements BaseEnum<String>, IEnum<String> {

    TECH_PARAM("key_3372d9d896d74aa8920934902869039b"),

    BASIC_PARAMETER("key_a9c707adaf9a406f91934de73d54764e"),

    EVENT_PARAM("key_e86a6ee48a37483da1395b8d278648f8"),

    EVENT_RECORD("key_45fe591b4a97493a9bbd6967722a1a5c"),

    RUN_PARAM("key_54fedcd29d9e414391c96fa0b36a6b4f"),

    CONTROL_PARAM("key_a052f5cc19ec45e58698e10844d4877e"),

    SETTING_PARAM("key_81de0ce05ddf41ff8eadaf385dc43241");

    /**
     * 枚举值
     */
    private final String value;

    @Override
    public String getValue() {
        return LocaleMessageUtil.getMessage(value);
    }

    @Override
    public String value() {
        return LocaleMessageUtil.getMessage(value);
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(value);
    }

}
