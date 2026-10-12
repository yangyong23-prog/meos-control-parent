package com.persagy.apccontrol.basic.structure.demand.enumeration.lighting;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

/**
 * 需求管理-照明需求-类型配置模式枚举
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Getter
@AllArgsConstructor
public enum DemandLightingTypeSettingModeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 未配置
     */
    NOT_CONFIGURED("NOT_CONFIGURED", "key_a09d40c0ec674d9583b1c6b8c7f22ee1"),

    /**
     * 全开
     */
    ALL_OPEN("ALL_OPEN", "key_fa45e52eedb14483bf68ac6a2b93c1db"),

    /**
     * 全关
     */
    ALL_CLOSE("ALL_CLOSE", "key_4ff1b9aef71e47a295ae837ce389d0ab"),

    /**
     * 部分开启
     */
    PARTIAL_OPEN("PARTIAL_OPEN", "key_ce1ef8b88c3a473d9b562c9283bac45d"),

    /**
     * 编组配置
     */
    INSTANCE_GROUP("INSTANCE_GROUP", "key_6764747d7d844a8cbe74742ff2a0a5fc"),

    /**
     * 反馈调节
     */
    FEEDBACK_ADJUSTMENT("FEEDBACK_ADJUSTMENT", "key_a1b3ebc3093645f4933ba4b20f14b9de");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * 解析照明回路配置模式
     *
     * @return 照明回路配置模式
     * @since 7.250601.0
     */
    public @NonNull DemandLightingCircuitSettingModeEnum toCircuitSettingMode() {
        switch (this) {
            case ALL_OPEN:
                return DemandLightingCircuitSettingModeEnum.OPEN;
            case ALL_CLOSE:
                return DemandLightingCircuitSettingModeEnum.CLOSE;
            case PARTIAL_OPEN:
            case NOT_CONFIGURED:
                return DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED;
            case FEEDBACK_ADJUSTMENT:
                return DemandLightingCircuitSettingModeEnum.FEEDBACK_ADJUSTMENT;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(this.getClass().getSimpleName(), this);
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
    public static DemandLightingTypeSettingModeEnum analyze(String value) {
        return BaseEnum.analyze(value, DemandLightingTypeSettingModeEnum.class);
    }
}
