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
 * 运行日内部志关键词
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum RunningLogKeyWordEnum implements BaseEnum<String>, IEnum<String> {
    
    
    /**
     * 关键词
     */
    ADD("ADD", "key_123e778a64384b69905f6f256bf72493"),
    
    UPDATED("UPDATED", "key_ddc82084c21541ea820840e9338664b9"),
    
    DEL("DEL", "key_c6652b85ecbe4dd6ba39de79ecdee66a"),
    
    
    ADJUST("ADJUST", "key_bf6301133a294213b036069bdaaf275e"),
    
    ;
    
    
    @EnumValue
    private String code;
    
    private String desc;
    
    
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RunningLogKeyWordEnum resolve(String value) {
        if (value == null) {
            return null;
        }
        return BaseEnum.analyze(value, RunningLogKeyWordEnum.class);
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
