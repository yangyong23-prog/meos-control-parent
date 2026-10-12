package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description: 空间异常类型枚举
 * @author zyc
 * @date 2024/1/4 17:31
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum SpaceAbnormalTypeEnum implements BaseEnum<String>, IEnum<String> {

    R0("R0"),
    R1("R1"),
    L0("L0"),
    L1("L1"),
    RH1("RH1"),
    SS("SS")
    ;

    /**
     * 枚举值
     */
    private final String code;

    @Override
    public String getValue() {
        return code;
    }

    @Override
    public String value() {
        return code;
    }

    @Override
    public String getReasonPhrase() {
        return null;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SpaceAbnormalTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, SpaceAbnormalTypeEnum.class);
    }


}
