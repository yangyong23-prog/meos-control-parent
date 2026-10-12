package com.persagy.apccontrol.basic.model.enumtype.dtprwd.hot;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 物理世界中央供热系统信息点枚举
 *
 * @since v5.241001
 */
@Getter
@AllArgsConstructor
public enum DtpRwdCentralHotInfoCodeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 出水温度设定值
     */
    HOT_WATER_OUT_TEMP_SET("heatSecondarySideWaterMainSupplyTempSet", "key_4f8a97e09a7b414a83e2bdfaa80c5282"),

    /**
     * 供回水温差
     */

    heatSecondarySideWaterDetaTemp("heatSecondarySideWaterDetaTemp", "key_1e70170162094c229537a6019ae8ea72"),

    /**
     * 供回水温差设定（热源二次侧供回水温差设定）
     */
    heatSecondarySideWaterDetaTempSet("heatSecondarySideWaterDetaTempSet", "key_a6c925e14bb246bcb2da9f91447c3461"),


    /**
     * 供回水压差
     */
    heatSecondarySideWaterDetaPressure("heatSecondarySideWaterDetaPressure", "key_faae2419e0ab48f9aa2709f38c5dcea6"),

    /**
     * 供回水压差设定（热源二次侧供回水压差设定）
     */
    heatSecondarySideWaterDetaPressureSet("heatSecondarySideWaterDetaPressureSet", "key_2fe2d943de4a41f2a0c68f43e5a278ca");





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
    public static DtpRwdCentralHotInfoCodeEnum analyze(String value) {
        return BaseEnum.analyze(value, DtpRwdCentralHotInfoCodeEnum.class);
    }
}
