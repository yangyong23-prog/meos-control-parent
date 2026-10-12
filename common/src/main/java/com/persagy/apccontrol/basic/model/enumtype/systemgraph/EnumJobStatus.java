package com.persagy.apccontrol.basic.model.enumtype.systemgraph;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnumJobStatus implements BaseEnum<Integer>, IEnum<Integer> {

    SUCCESS(0, "key_423f54233e7e415f92ce5d27942881a0"),
    FAIL(1, "key_f2704e092ff44465b0b37f56e2fa03f6"),
    RUNNING(2, "key_1dc4ec08c49f435889463e356cafe549"),
    ;

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
