package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 干预任务配置的数据类型
 */
@Getter
@AllArgsConstructor
public enum InterventionTaskConfigSourceTypeEnum implements BaseEnum<String>, IEnum<String> {
    /**
     * 数据来源
     */
    WEB("WEB", "key_1d030ea985f64ce999ba9bd60e54f2f9"),
    APP("APP", "key_29381ad1697a4fb9ae86f3871ae2cf49"),
    ;

    private final String code;
    private final String desc;


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
        return LocaleMessageUtil.getMessage( desc);
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage( desc);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static InterventionTaskConfigSourceTypeEnum resolve(String value) {
        return BaseEnum.analyze(value, InterventionTaskConfigSourceTypeEnum.class);
    }
}
