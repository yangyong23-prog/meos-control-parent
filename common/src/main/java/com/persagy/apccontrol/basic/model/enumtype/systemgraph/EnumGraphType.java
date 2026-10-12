package com.persagy.apccontrol.basic.model.enumtype.systemgraph;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnumGraphType implements BaseEnum<Integer>, IEnum<Integer> {
    //0、原图，1、副本
    ORIGINAL(0, "key_ec1c9d8e0a7a4a8fbe4190b8dd105de1"),
    COPY(1, "key_3b441ceea2e24140a4a85595d47271bc");

    final Integer value;
    final String reasonPhrase;

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
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }
}
