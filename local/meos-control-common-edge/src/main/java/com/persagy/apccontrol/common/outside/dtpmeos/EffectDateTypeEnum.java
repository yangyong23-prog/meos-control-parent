package com.persagy.apccontrol.common.outside.dtpmeos;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 日期生效类型
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2025/4/03 10:30
 */
@Getter
@AllArgsConstructor
public enum EffectDateTypeEnum implements BaseEnum<String>, IEnum<String> {
    
    GENERAL("GENERAL", "key_be2d6baece214150bb6995e6f8f88909"),
    
    WEEK_TAG("WEEK_TAG", "key_300cdf314f7d452ca64c2545cda778d5"),
    
    WORK_TAG("WORK_TAG", "key_12e35e12fa3f41ba86080ec1224426a7");
    
    private String value;
    
    private String reasonPhrase;
    
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static EffectDateTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, EffectDateTypeEnum.class, false);
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
