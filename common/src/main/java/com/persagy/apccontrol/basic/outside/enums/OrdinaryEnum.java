package com.persagy.apccontrol.basic.outside.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrdinaryEnum implements BaseEnum<String> {
    E0("E0", 1, "常见"),
    E1("E1", 2, "偶发"),
    E2("E2", 3, "较为少见");

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
    public static OrdinaryEnum analyze(String code) {
        return BaseEnum.analyze(code, OrdinaryEnum.class);
    }
}
