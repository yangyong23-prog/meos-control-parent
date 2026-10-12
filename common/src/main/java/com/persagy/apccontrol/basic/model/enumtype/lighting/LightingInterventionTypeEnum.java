package com.persagy.apccontrol.basic.model.enumtype.lighting;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 照明-干预类型枚举
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Getter
@AllArgsConstructor
public enum LightingInterventionTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 人工干预需求
     */
    DEMAND_INTERVENTION("DEMAND_INTERVENTION", "key_8436aa8c395240939a68cad35b162743", "key_8436aa8c395240939a68cad35b162743"),

    /**
     * 设备手动控制
     */
    MANUAL_CONTROL("MANUAL_CONTROL", "key_870eafbde5ec457dbe2ce2e6f6c32292", "key_1907ad695d1c4c96a119498d926e8720");

    private final String value;
    private final String reasonPhrase;
    private final String viewValue;

    @Override
    public String getValue() {
        return this.value;
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
    public static LightingInterventionTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, LightingInterventionTypeEnum.class);
    }
}
