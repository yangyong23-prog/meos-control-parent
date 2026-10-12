package com.persagy.apccontrol.basic.structure.bizcheck.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *检查类型枚举
 *
 * @author wxf
 * @date 2026-02-27
 */
@Getter
@AllArgsConstructor
public enum BizCheckTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     *自动
     */
    AUTO("AUTO", "key_check_type_auto"),

    /**
     *人工
     */
    MANUAL("MANUAL", "key_check_type_manual");

    /**
     *枚值举值
     */
    private final String value;

    /**
     *枚信息信息
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

    @Override
    public String toString() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static BizCheckTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, BizCheckTypeEnum.class);
    }
}