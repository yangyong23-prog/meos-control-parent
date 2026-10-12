package com.persagy.apccontrol.basic.controller.dashboard;


import com.persagy.apccontrol.basic.constant.ExternalServerConstant;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.dashboard.DashboardBatchControlQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandSpaceByGroupFloorQueryParam;
import com.persagy.apccontrol.basic.model.vo.dashboard.DashboardBatchControlVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdEquipmentVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import com.persagy.apccontrol.basic.service.dashboard.DashboardGridChartService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/dashboardGridChart")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DashboardGridChartController {

    private final DashboardGridChartService dashboardService;

    @Operation(summary = "查询-网格图空间信息", tags = {
            VersionConstant.V5_241102_0,
            ExternalServerConstant.CUSTOM_YX
    })
    @PostMapping("/querySpace")
    public R<List<? extends DtpRwdSpaceVO>> querySpace(@RequestBody @Valid DemandSpaceByGroupFloorQueryParam param) {
        return R.success(dashboardService.querySpace(param));
    }

    @Operation(summary = "定制接口查询机房空间",tags = VersionConstant.V7_260802_0)
    @GetMapping("/querySpace/computerRoomSecurity")
    public R<List<? extends DtpRwdSpaceVO>> querySpaceComputerRoomSecurity() {
        return R.success(dashboardService.querySpaceComputerRoomSecurity());
    }

    @Operation(summary = "查询-网格图设备信息", tags = VersionConstant.V5_241202_0)
    @PostMapping("/queryEquipment")
    public R<List<? extends DtpRwdEquipmentVO>> queryEquipment(@RequestBody @Valid DemandSpaceByGroupFloorQueryParam param) {
        return R.success(dashboardService.queryEquipment(param));
    }


    @Operation(summary = "查询-批量控制", tags = VersionConstant.V7_250901_0)
    @PostMapping("/listBatchControl")
    public R<List<DashboardBatchControlVO>> listBatchControl(
            @RequestBody @Valid DashboardBatchControlQueryParam param) {
        return R.success(dashboardService.listBatchControl(param));
    }
}
