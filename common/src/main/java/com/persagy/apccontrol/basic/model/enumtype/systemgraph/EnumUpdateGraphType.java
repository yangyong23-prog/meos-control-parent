package com.persagy.apccontrol.basic.model.enumtype.systemgraph;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnumUpdateGraphType implements BaseEnum<Integer>, IEnum<Integer> {

    EQ(0, "设备"),
    PASSBY(1, "小附件"),
    GROUP(2, "组"),
    CUSTOMIZE(3, "自定义");

    private final Integer value;
    private final String desc;

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public Integer value() {
        return value;
    }

    @Override
    public String getReasonPhrase() {
        return desc;
    }
}
