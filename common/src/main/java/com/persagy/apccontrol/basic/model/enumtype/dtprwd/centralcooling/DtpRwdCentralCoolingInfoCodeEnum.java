package com.persagy.apccontrol.basic.model.enumtype.dtprwd.centralcooling;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 物理世界中央供冷系统信息点枚举
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Getter
@AllArgsConstructor
public enum DtpRwdCentralCoolingInfoCodeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 冷机出水温度设定值
     */
    CHILL_WATER_OUT_TEMP_SET("chillWaterOutTempSet", "key_0e9d167abce8422880636d2f36cb6550"),
    /**
     * 出水温度设定值-供热
     */
    HEAT_WATER_SUPPLY_TEMP_SET("heatWaterSupplyTempSet", "key_1297ea4f90b6454dacde02431a593d64"),
    /**
     * 冷冻一次侧供回水温差设定
     */
    CHILL_WATER_DETA_TEMP_SET("chillWaterDetaTempSet", "key_a6c925e14bb246bcb2da9f91447c3461"),

    /**
     * 冷冻一次一级泵供回水压差设定
     */
    CHILL_WATER_DETA_PRESS_SET("chillWaterDetaPressSet", "key_a197ede4d10142519c312375c9de7b23"),

    /**
     * 最不利末端压差
     */
    WORST_TERMINAL_DETA_PRESS_SET("worstTerminalDetaPressSet", "key_5ea47df019c442abaa2a1e9f498b68f6"),

    ;

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
    public static DtpRwdCentralCoolingInfoCodeEnum analyze(String value) {
        return BaseEnum.analyze(value, DtpRwdCentralCoolingInfoCodeEnum.class);
    }
}
