package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 运行分析信息点枚举
 */
@AllArgsConstructor
public enum RunAnalysisInfoCodeEnum implements BaseEnum<String>, IEnum<String> {

    SPACE_TDB("tdb", "key_7274b502eecc4af1ac679f90941dfcbe"),
    SPACE_CO2("cO2", "key_f4b2a3540e1543c9a24f9c15c564caaa"),
    ;
    @EnumValue
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
        return LocaleMessageUtil.getMessage(desc);
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(desc);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RunAnalysisInfoCodeEnum analyze(String value) {
        return BaseEnum.analyze(value, RunAnalysisInfoCodeEnum.class);
    }

    /**
     * 按根据运行监控二级类型获取
     *
     * @param secondTypeEnum 第二类枚举
     * @return {@link RunAnalysisInfoCodeEnum }
     */
    public static RunAnalysisInfoCodeEnum getBySecondType(@NotNull RunningMonitorSecondTypeEnum secondTypeEnum) {
        switch (secondTypeEnum) {
            case RUN_ANALYSIS_TDB:
                return SPACE_TDB;
            case RUN_ANALYSIS_CO2:
                return SPACE_CO2;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(RunAnalysisInfoCodeEnum.class.getSimpleName(), secondTypeEnum.getClass());
        }
    }
}
