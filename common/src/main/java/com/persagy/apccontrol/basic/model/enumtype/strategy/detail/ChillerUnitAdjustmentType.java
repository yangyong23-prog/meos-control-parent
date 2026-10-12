package com.persagy.apccontrol.basic.model.enumtype.strategy.detail;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 冷机调整原因类型
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Getter
@AllArgsConstructor
public enum ChillerUnitAdjustmentType implements BaseEnum<String>, IEnum<String> {

    /**
     * 室内过冷|整体供冷量过量
     */
    SUPERCOOLING("SUPERCOOLING", "key_223fc21f6d2d4eb6b11bbcef298bd83a"),

    /**
     * 临时调整冷冻水流量
     */
    TEMP_COOLING_SUPPLY_REDUCE("TEMP_ADJUSTMENT", "key_c5497a529b3e49519fa6c855a6b9db26"),

    /**
     * 局部过冷
     */
    PARTIAL_AREA_SUPERCOOLING("PARTIAL_AREA_SUPERCOOLING", "key_cc416037c39c40239b776ccc279f8377"),

    /**
     * 室内过热|整体供冷量不足
     */
    SUPERHEAT("SUPERHEAT", "key_e83a79cebea34e5e91cb2a0ff51306f8"),

    /**
     * 临时供冷需求增加
     */
    TEMP_COOLING_SUPPLY_INCREASE("TEMP_COOLING_SUPPLY_INCREASE", "key_0defc69c3caa430b8010585460b8e5f0"),

    /**
     * 局部过热
     */
    PARTIAL_AREA_SUPERHEAT("PARTIAL_AREA_SUPERHEAT", "key_a09e648e0e424fc69c38a29a129f0fa1"),

    /**
     * 减少供冷量
     */
    COOLING_REDUCE("COOLING_REDUCE", "key_8cc2460a13fc46a8bd4b64dc0043e079"),

    /**
     * 增加供冷量
     */
    COOLING_INCREASE("COOLING_INCREASE", "key_5ceee08468e641b7b848c26d02a5fbc3"),

    /**
     * 增强除湿能力
     */
    DEHUMIDIFICATION_INCREASE("DEHUMIDIFICATION_INCREASE", "key_52d6a54b2a4d41b79e925bc553f982ae"),

    /**
     * 临时调整冷冻水流量
     */
    ADJUST_FLOW("ADJUST_FLOW", "key_b05fbbb30ee94917924c0ba8cb2dc20a"),

    /**
     * 系统或设备自身的原因
     */
    SYS_EQ_SELF("SYS_EQ_SELF", "key_8903af50ac1d4b34b6b6f95d9d2ce1e6"),

    /**
     * 优化设定值
     */
    OPTIMIZE_SETVALUE("OPTIMIZE_SETVALUE", "key_98e65f55295c46f3947711b84a84f8d0"),

    /**
     * 优化设定值-热站
     */
    OPTIMIZE_SETVALUE_HEAT("OPTIMIZE_SETVALUE_HEAT", "key_6afb60b1013c4450b6055704ee60a4dd"),

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


    public static ChillerUnitAdjustmentType getEnumByCode(String code) {
        if (code == null) {
            return null;
        }
        for (ChillerUnitAdjustmentType i : ChillerUnitAdjustmentType.values()) {
            if (code.equals(i.value())) {
                return i;
            }
        }
        return null;
    }


    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ChillerUnitAdjustmentType analyze(String value) {
        return BaseEnum.analyze(value, ChillerUnitAdjustmentType.class);
    }
}
