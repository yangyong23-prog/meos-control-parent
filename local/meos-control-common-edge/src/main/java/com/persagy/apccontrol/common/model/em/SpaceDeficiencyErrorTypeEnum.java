package com.persagy.apccontrol.common.model.em;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SpaceDeficiencyErrorTypeEnum  implements BaseEnum<String>, IEnum<String> {

    HIGN("HIGH", "key_0e32ed2304dd45aeb871f81bd7de4e53"),
    LOW("LOW", "key_75518acec27148f7a54ebc3d2bb16cae");
    private String code;
    private String desc;

    public void setCode(String code) {
        this.code = code;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String getValue() {
        return this.code;
    }

    @Override
    public String value() {
        return this.code;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(this.desc);
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(this.desc);
    }
}
