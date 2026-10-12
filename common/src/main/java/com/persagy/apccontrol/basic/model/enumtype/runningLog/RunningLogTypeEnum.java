package com.persagy.apccontrol.basic.model.enumtype.runningLog;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum RunningLogTypeEnum implements BaseEnum<Integer>, IEnum<Integer> {

    /**
     *
     */
    AUTO(1, "key_e31ea221b6e74c5898d5ed6208968a54"),
    MANUAL(2, "key_c0293c3c5576420c9989dd329fd8ce09"),
    
    ;

    private final Integer code;

    private final String desc;

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

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RunningLogTypeEnum resolve(Integer value) {
        if (value == null) {
            return null;
        }
        return BaseEnum.analyze(value, RunningLogTypeEnum.class);
    }

    public static Map<String, String> getRunningLogTypeMap() {
        RunningLogTypeEnum[] values = RunningLogTypeEnum.values();
        Map<String, String> resultMap = new HashMap<>();
        for (RunningLogTypeEnum runningLogTypeEnum : values) {
            resultMap.put(runningLogTypeEnum.getCode() + "", runningLogTypeEnum.getDesc());
        }
        return  resultMap;
    }

}
