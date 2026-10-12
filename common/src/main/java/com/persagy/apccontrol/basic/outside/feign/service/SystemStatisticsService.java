package com.persagy.apccontrol.basic.outside.feign.service;

import com.persagy.apccontrol.basic.model.vo.outside.energy.*;

/**
 * @Author 薛涛
 * @Description 系统统计的对外接口
 * @Date 14:55 2024/8/31
 * @Param 
 * @return 
 **/
public interface SystemStatisticsService {

    SubstationStatistics substationAndDistributionStatistics(String projectId);

    StatisticsPassengerFlowVO statisticsPassengerFlow(String projectId) throws Exception;

    WaterSupplyDrainageStatisticsVO waterSupplyDrainageStatistics(String projectId);

    SecurityStatisticsVO parkingLotStatistics(String projectId);

    Object improveAirCirculationStatistics(String projectId);

    SecurityStatisticsVO securityProtectionStatistics(String projectId);

    ElevatorStatisticsVO elevatorStatistics(String projectId);
}
