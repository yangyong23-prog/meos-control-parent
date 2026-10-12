package com.persagy.apccontrol.basic.model.enumtype.parking;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum ChargingRateOptionalEnum implements BaseEnum<String>, IEnum<String> {
    FAST_CHARGING("fastCharging", "key_231ca1b7f7004097a123d95de636b94d"),
    NORMAL_CHARGING("normalCharging", "key_e283ce09b33e48bdace3f78051754b7f");


    final String value;
    final String reasonPhrase;


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
        return null;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ChargingRateOptionalEnum analyze(String value) {
        return BaseEnum.analyze(value, ChargingRateOptionalEnum.class);
    }


    public static ChargingRateOptionalEnum getChargingRate(Object value) {
        return "1".equals(value) ? FAST_CHARGING : NORMAL_CHARGING;
    }
}
