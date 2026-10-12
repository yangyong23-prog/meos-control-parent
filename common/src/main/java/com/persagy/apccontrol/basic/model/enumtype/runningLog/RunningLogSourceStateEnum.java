package com.persagy.apccontrol.basic.model.enumtype.runningLog;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 运行日志来源类型枚举
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum RunningLogSourceStateEnum implements BaseEnum<Integer>, IEnum<Integer> {
    
    /**
     * 来源类型
     */
    UNKNOWN(-1, "key_3fbe7aabf64645c8a05c9cadbd593144"),
    UNFINISHED(0, "key_56e1f276b8174847a0a2df8dc535b876"),
    SUCCEED(1, "key_423f54233e7e415f92ce5d27942881a0"),
    FAIL(2, "key_f2704e092ff44465b0b37f56e2fa03f6"),
    TIMEOUT(3, "key_14a1210127964b1a89c57ad87a488f43"),
    SUSPEND(4, "key_ff45adcc17174ad4a0c03ab969dc8fb0"),
    ERROR(5, "key_5bf0b8410a1f4947ab5c4d9c580e3db4"),
    
    ;
    
    
    @EnumValue
    private Integer code;
    
    private String desc;
    
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RunningLogSourceStateEnum resolve(Integer value) {
        if (value == null) {
            return null;
        }
        return BaseEnum.analyze(value, RunningLogSourceStateEnum.class);
    }
    
    @Override
    public Integer getValue() {
        return this.code;
    }
    
    @Override
    public Integer value() {
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
