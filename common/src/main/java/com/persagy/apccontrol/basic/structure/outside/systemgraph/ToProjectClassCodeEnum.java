package com.persagy.apccontrol.basic.structure.outside.systemgraph;

import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ToProjectClassCodeEnum implements BaseEnum<String> {

    WSDW("WSDW", "给水机房关系图"),
    FFSR("FFSR", "自动喷水灭火系统关系图");

    String value;
    String graphType;

    @Override
    public String value() {
        return value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(graphType);
    }

    public String getGraphType() {
        return LocaleMessageUtil.getMessage(graphType);
    }
}
