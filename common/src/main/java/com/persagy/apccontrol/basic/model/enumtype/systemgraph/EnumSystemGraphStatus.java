package com.persagy.apccontrol.basic.model.enumtype.systemgraph;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnumSystemGraphStatus implements BaseEnum<Integer>, IEnum<Integer> {
    //状态：未发布，已发布，已下架
    UN_PUBLISHED("key_7e44aba634e84ae5b1d488c4974c6df0", 0),
    PUBLISHED("key_da65208dd7c44543bfb7eb873ec21400", 1),
    OFF_LINE("key_7ceb71cedb8845cc91d0b55b48c13f75", 2);
    final String reasonPhrase;
    final Integer value;

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
