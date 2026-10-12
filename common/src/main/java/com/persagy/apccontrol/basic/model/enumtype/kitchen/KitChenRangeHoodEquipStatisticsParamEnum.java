package com.persagy.apccontrol.basic.model.enumtype.kitchen;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum KitChenRangeHoodEquipStatisticsParamEnum implements BaseEnum<String>, IEnum<String> {

    ACKLSF_OBJIDS("acklsfObjIds", "key_9ace05b4d3e641d7a65df40fdd4a6d0e"),
    ACATFU_OBJIDS("acatfuObjIds", "key_feab127d425e4d2aaa15aa1cacd126ed"),
    ACVTSF_OBJIDS("acvtsfObjIds", "key_52ae4cc241a44fc8bdb375e42a1b3518"),
    ACKLAP_OBJIDS("acklapObjIds", "key_810d8f7c97824ca781038c950dedefdb");

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
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }
}
