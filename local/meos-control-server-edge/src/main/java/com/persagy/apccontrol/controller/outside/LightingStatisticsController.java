package com.persagy.apccontrol.controller.outside;

import com.persagy.apccontrol.basic.model.enumtype.outside.energy.StatisticsTypeEnum;
import com.persagy.apccontrol.basic.model.vo.outside.energy.StatisticsVO;
import com.persagy.apccontrol.service.LightingStatisticsService;
import com.persagy.framework.common.entity.R;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.persagy.apccontrol.basic.constant.ExternalServerConstant.ENERGY_DDP_SERVER;
import static com.persagy.apccontrol.basic.constant.ExternalServerConstant.ENERGY_DDP_SERVER_EDGE;
import static com.persagy.apccontrol.basic.constant.VersionConstant.V5_240801_0;

@Api(value = "照明统计外部接口", tags = V5_240801_0)
@RestController
@RequestMapping("/statistics")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LightingStatisticsController {

    private final LightingStatisticsService lightsStatisticsService;

    @Operation(summary = "获取照明统计信息", tags = {V5_240801_0, ENERGY_DDP_SERVER, ENERGY_DDP_SERVER_EDGE})
    @GetMapping("/getLightingStatistics")
    public R<StatisticsVO> getStatistics(@RequestParam(value = "type") StatisticsTypeEnum type) {
        return R.success(lightsStatisticsService.getLightingStatistics(type));
    }

}
