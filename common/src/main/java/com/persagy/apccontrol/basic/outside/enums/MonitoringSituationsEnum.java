package com.persagy.apccontrol.basic.outside.enums;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MonitoringSituationsEnum implements BaseEnum<String> {
    STRATEGY_EXECUTION_NOT_EXPECTED("STRATEGY_EXECUTION_NOT_EXPECTED", "策略执行不符预期"),
    IOT_DISCONNECTION("IOT_DISCONNECTION", "IOT失联"),
    STRATEGY_LOW_FREQUENCY("STRATEGY_LOW_FREQUENCY", "策略频次过低");

    private final String code;
    private final String desc;

    @Override
    public String value() {
        return code;
    }

    @Override
    public String getReasonPhrase() {
        return desc;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static MonitoringSituationsEnum analyze(String code) {
        return BaseEnum.analyze(code, MonitoringSituationsEnum.class);
    }

}
