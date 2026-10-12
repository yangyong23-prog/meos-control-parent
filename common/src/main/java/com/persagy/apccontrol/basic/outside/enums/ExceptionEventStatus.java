package com.persagy.apccontrol.basic.outside.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 异常事件状态
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/7/19 14:34
 * @since v5.240502.0
 */
@AllArgsConstructor
@Getter
public enum ExceptionEventStatus implements BaseEnum<String> {

    UNRESOLVED( 1, "key_5e4ebb0fae9d42cd8db8f2f363274b98"),
    RESOLVED( 2, "key_2834c0450b8d48b1b02b02fe936c3017");

    @EnumValue
    private final Integer value;
    private final String desc;

    @Override
    public String value() {
        return name();
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(desc);
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(desc);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ExceptionEventStatus analyze(String value) {
        return BaseEnum.analyze(value, ExceptionEventStatus.class);
    }
}
