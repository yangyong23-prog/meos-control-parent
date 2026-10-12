package com.persagy.apccontrol.basic.structure.demand.enumeration.lighting;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 需求管理-照明需求-照明回路配置模式枚举
 *
 * @author LiuYang
 * @since v5.2402.2
 */
@Getter
@AllArgsConstructor
public enum DemandLightingOpenCloseEnum implements BaseEnum<String>, IEnum<String> {


    /**
     * OPEN
     */
    NEW_OPEN("NEW_OPEN", "key_ebc6a171adc942b8950564761a2a4a42"),

    /**
     * 全关
     */
    NEW_CLOSE("NEW_CLOSE", "key_0559cb9bc3974d629ca949f7b66fa226");

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
    public static DemandLightingOpenCloseEnum analyze(String value) {
        return BaseEnum.analyze(value, DemandLightingOpenCloseEnum.class);
    }


}
