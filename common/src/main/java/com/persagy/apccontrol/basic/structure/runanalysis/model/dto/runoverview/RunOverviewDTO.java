package com.persagy.apccontrol.basic.structure.runanalysis.model.dto.runoverview;

import com.persagy.apccontrol.basic.model.vo.runanalysis.energy.RunOverviewEnergyAnalysisDto;
import com.persagy.apccontrol.basic.model.vo.runanalysis.energy.RunOverviewEnergyBudgetDto;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.RunAnalysisReportDataDto;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.common.WeatherDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 运行概述 DTO
 *
 * @author JiYinchuan
 * @since 7.260102.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RunOverviewDTO extends RunAnalysisReportDataDto {

    /**
     * 项目用电量
     */
    private RunOverviewEnergyAnalysisDto projectElectricity;

    /**
     * 当日预算使用率
     */
    private RunOverviewEnergyBudgetDto dayBudgetUtilizationRate;

    /**
     * 天气数据
     */
    private WeatherDTO weather;

    /**
     * 模版名称
     */
    private String templateName;

    /**
     * 价值选择类型
     */
    private String valueSelectType;

    /**
     * 空间环境品质
     */
    private SpaceEnvironmentalQualityDTO spaceEnvironmentalQuality;

    /**
     * 系统运行结果
     */
    private List<SystemRunningResultDTO> systemRunningResults;

}
