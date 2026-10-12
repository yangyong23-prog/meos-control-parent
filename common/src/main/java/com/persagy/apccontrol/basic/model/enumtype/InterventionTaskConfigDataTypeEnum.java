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
public enum InterventionTaskConfigDataTypeEnum implements BaseEnum<Integer>, IEnum<Integer> {
    /**
     * iot数据类型
     */
    MASTER(0, "key_f291a08972df45a1a37be9661785263a"),
    RELATION(1, "key_48f1a44e30634be09f01e3926e7955d1"),
    ;

    private final Integer code;
    private final String desc;


    @Override
    public Integer getValue() {
        return code;
    }

    @Override
    public Integer value() {
        return code;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(desc);
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(desc);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static InterventionTaskConfigDataTypeEnum resolve(Integer value) {
        return BaseEnum.analyze(value, InterventionTaskConfigDataTypeEnum.class);
    }
}
