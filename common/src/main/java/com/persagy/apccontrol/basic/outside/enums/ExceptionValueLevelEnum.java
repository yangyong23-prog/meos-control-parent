package com.persagy.apccontrol.basic.outside.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionValueLevelEnum implements BaseEnum<String> {

    HIGH("High", 1, "高"),
    MID("Mid", 2, "中"),
    LOW("Low", 3, "低");

    private final String code;
    private final int value;
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
    public static ExceptionValueLevelEnum analyze(String code) {
        return BaseEnum.analyze(code, ExceptionValueLevelEnum.class);
    }
}
