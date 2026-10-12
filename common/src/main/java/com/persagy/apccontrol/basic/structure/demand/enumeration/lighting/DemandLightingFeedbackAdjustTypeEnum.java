package com.persagy.apccontrol.basic.structure.demand.enumeration.lighting;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 需求管理-照明需求-反馈调节配置类型
 *
 * @author JiYinchuan
 * @since 7.251202.0
 */
@Getter
@AllArgsConstructor
public enum DemandLightingFeedbackAdjustTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 室内照度
     */
    INDOOR_ILLUMINANCE("INDOOR_ILLUMINANCE", "室内照度"),

    /**
     * 室外照度
     */
    OUTDOOR_ILLUMINANCE("OUTDOOR_ILLUMINANCE", "室外照度");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * 解析照度反馈调节类型指令
     *
     * @return 照度反馈调节类型指令
     * @since 7.251202.0
     */
    public BigDecimal parseIllumFbCtrTypeOrder() {
        switch (this) {
            case INDOOR_ILLUMINANCE:
                return BigDecimal.valueOf(1);
            case OUTDOOR_ILLUMINANCE:
                return BigDecimal.valueOf(2);
            default:
                return BigDecimal.valueOf(3);
        }
    }

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
    public static DemandLightingFeedbackAdjustTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, DemandLightingFeedbackAdjustTypeEnum.class);
    }
}
