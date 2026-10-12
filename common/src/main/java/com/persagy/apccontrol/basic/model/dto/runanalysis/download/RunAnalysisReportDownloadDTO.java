package com.persagy.apccontrol.basic.model.dto.runanalysis.download;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 运行分析报告下载DTO - 包含所有章节数据
 *
 * @author: system
 * @create: 2026-03-17
 */
@Data
public class RunAnalysisReportDownloadDTO {

    /**
     * 项目基础信息
     */
    private ProjectBasicInfoDTO projectBasicInfo;

    /**
     * 日汇总数据
     */
    private DailySummaryDataDTO dailySummaryData;

    /**
     * 冷站日汇总数据列表
     */
    private List<CoolingStationDailySummaryDTO> coolingStationDailySummaryList;

    /**
     * 冷站参考相似日能耗数据列表
     */
    private List<CoolingStationSimilarDayDTO> coolingStationSimilarDayList;

    /**
     * 逐15min冷站数据列表
     */
    private List<CoolingStation15MinDataDTO> coolingStation15MinDataList;

    /**
     * 逐15min冷站服务空间的温度状态列表
     */
    private List<ServiceSpaceTempStatusDTO> serviceSpaceTempStatusList;

    /**
     * 逐15min冷机数据列表
     */
    private List<Chiller15MinDataDTO> chiller15MinDataList;

    /**
     * 逐15min冷却塔风机数据列表
     */
    private List<CoolingTowerFan15MinDataDTO> coolingTowerFan15MinDataList;

    /**
     * 室外天气数据列表
     */
    private List<OutdoorWeatherDataDTO> outdoorWeatherDataList;
}
