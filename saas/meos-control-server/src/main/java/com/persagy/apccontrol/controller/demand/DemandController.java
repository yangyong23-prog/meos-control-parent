package com.persagy.apccontrol.controller.demand;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.demand.*;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.common.service.demand.DemandService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 需求管理控制器
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@RestController("SaasDemandController")
@RequestMapping("/demand")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandController<T extends BaseDemandConfig> {

    private final DemandService<T> demandService;

    @Operation(summary = "需求引用设置", tags = VersionConstant.V5_240902_0)
    @PostMapping("/referenceSettings")
    public R<Boolean> demandReferenceSettings(@RequestBody @Validated DemandReferenceParam param) {
        demandService.demandReferenceSettings(param);
        return R.success(true);
    }

    @Operation(summary = "需求清除配置", tags = VersionConstant.V5_240902_0)
    @PostMapping("/clearSettings")
    public R<Boolean> demandClearSettings(@RequestBody @Validated DemandClearParam param) {
        demandService.demandClearSettings(param);
        return R.success(true);
    }

    @Operation(summary = "新增-实例配置", tags = VersionConstant.V5_241102_0)
    @PostMapping("/addInstanceConfig")
    public R<Boolean> addInstanceConfig(@RequestBody @Valid DemandInstanceConfigAddOrEditParam param) {
        return R.success(demandService.addInstanceConfig(param));
    }

    @Operation(summary = "新增-需求配置", tags = VersionConstant.V5_240902_0)
    @PostMapping("/addDemandConfig")
    public R<Object> addDemandConfig(@RequestBody @Valid DemandAddParam param) {
        return R.success(demandService.addDemandConfig(param));
    }

    @Operation(summary = "复制-复制需求配置", tags = VersionConstant.V7_250902_0)
    @PostMapping("/copyConfigs")
    public R<Boolean> copyConfigs(@RequestBody @Valid DemandConfigCopyParam param) {
        return R.success(demandService.copyConfigs(param));
    }

    @Operation(summary = "校验-配置分组名称是否存在", tags = VersionConstant.V7_250202_0)
    @PostMapping("/checkConfigGroupNameExists")
    public R<Boolean> checkConfigGroupNameExists(@RequestBody @Valid DemandConfigGroupNameCheckParam param) {
        return R.success(demandService.checkConfigGroupNameExists(param));
    }

    @Operation(summary = "校验-配置分组名称是否存在", tags = VersionConstant.V5_240902_0)
    @PostMapping("/clearAirConditioningSpaceRoomFuncTypes")
    public R<Boolean> clearAirConditioningSpaceRoomFuncTypes() {
        return R.success(demandService.clearAirConditioningSpaceRoomFuncTypes());
    }
}
