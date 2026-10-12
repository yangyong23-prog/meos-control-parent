package com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject;

import lombok.Data;

/**
 * @Author weiyizhong
 * @Date 2026/2/3 11:20
 * @Company yushu.biz
 * @Description: 能耗-预算配置
 */
@Data
public class EnergyBudgetConfig extends RunAnalysisReportConfig{
    /**
     * 基准值
     */
    private String benchmark;
}
