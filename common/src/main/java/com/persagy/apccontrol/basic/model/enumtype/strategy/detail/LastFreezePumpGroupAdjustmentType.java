package com.persagy.apccontrol.basic.model.enumtype.strategy.detail;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 冷冻泵组调整原因类型
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Getter
@AllArgsConstructor
public enum LastFreezePumpGroupAdjustmentType implements BaseEnum<String>, IEnum<String> {

    /**
     * 系统或设备自身原因, 冷冻水流量长期调整
     */
    SELF_PROBLEM("SELF_PROBLEM", "key_4e6317e26b7f4beb91d580ca77c0d840"),

    /**
     * 临时调整冷冻水流量
     */
    TEMP_ADJUSTMENT("TEMP_ADJUSTMENT", "key_b05fbbb30ee94917924c0ba8cb2dc20a"),

    /**
     * 其他
     */
    OTHER_REASON("OTHER", "key_7d6652fc005d47b3854cdc48b0b5d4f7");

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

    public static LastFreezePumpGroupAdjustmentType getEnumByCode(String code) {
        if (code == null) {
            return null;
        }
        for (LastFreezePumpGroupAdjustmentType i : LastFreezePumpGroupAdjustmentType.values()) {
            if (code.equals(i.value())) {
                return i;
            }
        }
        return null;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static LastFreezePumpGroupAdjustmentType analyze(String value) {
        return BaseEnum.analyze(value, LastFreezePumpGroupAdjustmentType.class);
    }
}
