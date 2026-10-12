package com.persagy.apccontrol.common.outside.dtpmeos;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 日志数据赋予的目标
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2025/4/21 10:34
 */
@Getter
@AllArgsConstructor
public enum RunningDataTargetEnum implements BaseEnum<String>, IEnum<String> {
    
    SCHEDULE("SCHEDULE", "key_2025d3e1fd7342e8a8546bc4ced3ee37"),
    
    COMMAND("COMMAND", "key_557f1860644647b3919888d154dab121"),
    
    INTERVENTION("INTERVENTION", "key_2da8fed128f34402ac30f3531b25cb7f"),
    
    
    CHANGE_INTERVENTION_TIME("CHANGE_INTERVENTION_TIME", "key_26bd18d9c2404a4e851be30d35aaa5e5"),
    
    ;
    
    private String value;
    
    private String reasonPhrase;
    
    
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RunningDataTargetEnum analyze(String value) {
        return BaseEnum.analyze(value, RunningDataTargetEnum.class, false);
    }
    
    @Override
    public String value() {
        return value;
    }
    
    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }
    
}

