package com.persagy.apccontrol.basic.structure.runanalysis.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RunAnalysisReportTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 日报
     */
    DAILY("daily", "key_daily_report"),

    /**
     * 月报
     */
    MONTHLY("monthly", "key_monthly_report"),
    ;

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;


    @Override
    public String value() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RunAnalysisReportTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, RunAnalysisReportTypeEnum.class);
    }
}
