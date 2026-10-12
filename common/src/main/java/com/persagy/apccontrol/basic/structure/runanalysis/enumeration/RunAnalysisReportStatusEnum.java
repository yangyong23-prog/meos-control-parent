package com.persagy.apccontrol.basic.structure.runanalysis.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RunAnalysisReportStatusEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 确认
     */
    CONFIRM("confirm", "confirm"),

    /**
     * 未确认
     */
    UNCONFIRM("unconfirm", "unconfirm"),

    /**
     * 构建中
     */
    BUILDING("building", "building"),

    /**
     * 生成失败
     */
    FAILURE("failure", "failure"),
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
        return reasonPhrase;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RunAnalysisReportStatusEnum analyze(String value) {
        return BaseEnum.analyze(value, RunAnalysisReportStatusEnum.class);
    }
}
