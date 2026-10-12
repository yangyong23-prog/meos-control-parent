package com.persagy.apccontrol.basic.structure.demand.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 需求配置取消引用类型枚举
 */
@Getter
@AllArgsConstructor
public enum DemandConfigCancelReferenceTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 取消引用并保留配置
     */
    RETAIN_CONFIG("RETAIN_CONFIG", "key_d9fa07616c874c0eb385733f8af65beb"),

    /**
     * 取消引用并清除配置
     */
    DELETE_CONFIG("DELETE_CONFIG", "key_fa5511de9c2045b79b361386b5f2e855");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
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

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DemandConfigCancelReferenceTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, DemandConfigCancelReferenceTypeEnum.class);
    }
}
