package com.persagy.apccontrol.basic.structure.runanalysis.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject.*;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RunAnalysisReportChapterEnum implements BaseEnum<String>, IEnum<String> {

    REPORT_CHAPTER_RUN_OVERVIEW("report_chapter_run_overview", "运行概述", RunAnalysisReportConfig.class),

    REPORT_CHAPTER_ENERGY_ANALYSIS("report_chapter_energy_analysis", "能耗分析", EnergyAnalysisConfig.class),

    REPORT_CHAPTER_ENERGY_BUDGET("report_chapter_energy_budget", "能耗预算", EnergyBudgetConfig.class),

    REPORT_CHAPTER_AC("report_chapter_ac", "空调", RunAnalysisReportConfig.class),

    REPORT_CHAPTER_COOLING_ANALYSIS("report_chapter_cooling_analysis", "冷量分析", RunAnalysisReportConfig.class),

    REPORT_CHAPTER_COOLING_TEMP_TARGET("report_chapter_cooling_temp_target", "空间温度目标", RunAnalysisReportConfig.class),

    REPORT_CHAPTER_COOLING_ENERGY("report_chapter_cooling_energy", "冷量及能耗", RunAnalysisReportConfig.class),

    REPORT_CHAPTER_ENV_QUALITY("report_chapter_env_quality", "环境品质", EnvQualityConfig.class),

    REPORT_CHAPTER_COOLING_SYS_RUN("report_chapter_cooling_sys_run", "冷站系统运行", RunAnalysisReportConfig.class),

    REPORT_CHAPTER_COOLING_SYS_RUN_ITEM("report_chapter_cooling_sys_run_item", "冷站系统实例", RunAnalysisReportACConfig.class),
    ;

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * 配置模型
     */
    private final Class<? extends RunAnalysisReportConfig> configModel;


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
    public static RunAnalysisReportChapterEnum analyze(String value) {
        return BaseEnum.analyze(value, RunAnalysisReportChapterEnum.class);
    }
}
