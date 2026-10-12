package com.persagy.apccontrol.basic.controller.dashboard;

import com.persagy.apccontrol.basic.model.param.dashboard.DashBoardQueryCountParam;
import com.persagy.apccontrol.basic.model.vo.dashboard.LightingCountDataVo;
import com.persagy.apccontrol.basic.model.vo.dashboard.LightingEquipmentCountDataVo;
import com.persagy.apccontrol.basic.service.dashboard.DashboardLightingService;
import com.persagy.apccontrol.basic.service.lighting.AbstractLightingService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 照明Dashboard
 *
 * @author : wangchenghong
 * @date : 2024/12/3 15:46
 */
@RestController
@RequestMapping(value = "/lighting")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DashboardLightingController {

    private final DashboardLightingService dashboardLightingService;
    @Resource
    AbstractLightingService lightingService;

    @Operation(summary = "查询-Dashboard")
    @PostMapping("/dashboard")
    public R<LightingCountDataVo> dashboard(@RequestBody @Valid DashBoardQueryCountParam dashBoardQueryCountParam) {
        return R.success(lightingService.dashboard(dashBoardQueryCountParam));
    }

    @Operation(summary = "查询-Dashboard设备统计")
    @PostMapping("/dashboardEquipmentStatistics")
    public R<LightingEquipmentCountDataVo> dashboardEquipmentStatistics() {
        return R.success(dashboardLightingService.dashboardEquipmentStatistics());
    }
    

}
