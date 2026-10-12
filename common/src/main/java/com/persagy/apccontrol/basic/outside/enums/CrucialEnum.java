package com.persagy.apccontrol.basic.outside.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CrucialEnum implements BaseEnum<String> {

    PIVOTAL("PIVOTAL", "关键"),
    OTHER("OTHER", "其他");

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
    public static CrucialEnum analyze(String code) {
        return BaseEnum.analyze(code, CrucialEnum.class);
    }
}
