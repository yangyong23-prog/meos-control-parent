package com.persagy.apccontrol.basic.model.vo.runanalysis;

import lombok.Data;

import java.util.List;

@Data
public class RunAnalysisStatisticsVO {


    /**
     * 价值选择
     */
    private ValueSelectionVO valueSelection;
    /**
     * 能耗统计列表
     */
    private EnergyStatisticsVO energyStatistics;
    /**
     * 冷站效率指标
     */
    private List<SystemEfficiencyVO> systemEfficiencyList;
//    /**
//     * 冷热源效率指标
//     */
//    private List<ColdEfficiencyVO> coldHeatEfficiency;
}
