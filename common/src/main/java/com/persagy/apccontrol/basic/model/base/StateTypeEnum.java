package com.persagy.apccontrol.basic.model.base;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.Getter;

/**
 * @author yuyao
 * @Date 2022/3/2 2:05 PM
 * @Description 状态枚举
 */
@Getter
public enum StateTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 有效
     */
    VALID("valid", "key_5860569a36684b37a22a67d084175020"),

    /**
     * 无效
     */
    INVALID("invalid", "key_8a73984f66ed442ca1435e48e0123c95");

    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String explain;

    StateTypeEnum(String code, String explain) {
        this.code = code;
        this.explain = explain;
    }

    @Override
    public String getValue() {
        return this.code;
    }

    @Override
    public String value() {
        return this.code;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(this.explain);
    }

    public String getExplain() {
        return LocaleMessageUtil.getMessage(this.explain);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static StateTypeEnum resolve(String value) {
        return BaseEnum.analyze(value, StateTypeEnum.class, false);
    }
}
