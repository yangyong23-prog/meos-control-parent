package com.persagy.apccontrol.common.logtrace.enums;


import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

public enum TemplateHeaderType {

    FIXED(1,"key_958e2d1e934a4459a9e358e88c3e6373"),
    DYNAMIC(2,"key_3e5b29aa155f4c4798906a7c6b59bb1f");

    private int code;
    private String desc;


    TemplateHeaderType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(desc);
    }
}
