package com.persagy.apccontrol.basic.controller.outside;

import com.persagy.apccontrol.basic.model.vo.outside.energy.*;
import com.persagy.apccontrol.basic.outside.feign.service.SystemStatisticsService;
import com.persagy.framework.common.entity.R;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.persagy.apccontrol.basic.constant.ExternalServerConstant.ENERGY_DDP_SERVER;
import static com.persagy.apccontrol.basic.constant.ExternalServerConstant.ENERGY_DDP_SERVER_EDGE;
import static com.persagy.apccontrol.basic.constant.VersionConstant.V5_240802_0;

@Api(value = "系统统计外部接口", tags = V5_240802_0)
@RestController
@RequestMapping("/external/systemStatistics")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SystemStatisticsController {

    private final SystemStatisticsService systemStatisticsService;

    @Operation(summary = "变配电系统数据统计", tags = {V5_240802_0, ENERGY_DDP_SERVER,ENERGY_DDP_SERVER_EDGE})
    @PostMapping("/substationAndDistributionStatistics")
    public R<SubstationStatistics> substationAndDistributionStatistics(@RequestHeader String projectId) {
        return R.success(systemStatisticsService.substationAndDistributionStatistics(projectId));
    }
    @Operation(summary = "客流数据统计", tags = {V5_240802_0, ENERGY_DDP_SERVER,ENERGY_DDP_SERVER_EDGE})
    @PostMapping("/statisticsPassengerFlow")
    public R<StatisticsPassengerFlowVO> statisticsPassengerFlow(@RequestHeader String projectId) throws Exception {
        return R.success(systemStatisticsService.statisticsPassengerFlow(projectId));
    }
    @Operation(summary = "给排水数据统计", tags = {V5_240802_0, ENERGY_DDP_SERVER,ENERGY_DDP_SERVER_EDGE})
    @PostMapping("/waterSupplyDrainageStatistics")
    public R<WaterSupplyDrainageStatisticsVO> waterSupplyDrainageStatistics(@RequestHeader String projectId) {
        return R.success(systemStatisticsService.waterSupplyDrainageStatistics(projectId));
    }
    @Operation(summary = "停车场数据统计", tags = {V5_240802_0, ENERGY_DDP_SERVER,ENERGY_DDP_SERVER_EDGE})
    @PostMapping("/parkingLotStatistics")
    public R<SecurityStatisticsVO> parkingLotStatistics(@RequestHeader String projectId) {
        return R.success(systemStatisticsService.parkingLotStatistics(projectId));
    }
    @Operation(summary = "通风系统数据统计（暂不作）", tags = {V5_240802_0, ENERGY_DDP_SERVER,ENERGY_DDP_SERVER_EDGE})
    @PostMapping("/improveAirCirculationStatistics")
    public R<Object> improveAirCirculationStatistics(@RequestHeader String projectId) {
        return R.success(systemStatisticsService.improveAirCirculationStatistics(projectId));
    }
    @Operation(summary = "安防系统数据统计", tags = {V5_240802_0, ENERGY_DDP_SERVER,ENERGY_DDP_SERVER_EDGE})
    @PostMapping("/securityProtectionStatistics")
    public R<SecurityStatisticsVO> securityProtectionStatistics(@RequestHeader String projectId) {
        return R.success(systemStatisticsService.securityProtectionStatistics(projectId));
    }
    @Operation(summary = "电梯数据统计", tags = {V5_240802_0, ENERGY_DDP_SERVER,ENERGY_DDP_SERVER_EDGE})
    @PostMapping("/elevatorStatistics")
    public R<ElevatorStatisticsVO> elevatorStatistics(@RequestHeader String projectId) {
        return R.success(systemStatisticsService.elevatorStatistics(projectId));
    }



}
