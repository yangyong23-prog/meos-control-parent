package com.persagy.apccontrol.controller.lighting;

import com.persagy.apccontrol.basic.model.param.lighting.LightingControllerWithCircuitQueryParam;
import com.persagy.apccontrol.basic.model.param.lighting.LightingOperatingParametersSetParam;
import com.persagy.apccontrol.basic.model.vo.rules.SetOperatingParametersVO;
import com.persagy.apccontrol.basic.outside.dto.lighting.LightingDataDto;
import com.persagy.apccontrol.common.model.dto.RunningModeLightingDTO;
import com.persagy.apccontrol.common.service.lighting.LightingService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.persagy.apccontrol.basic.constant.VersionConstant.V5_2404_2;
import static com.persagy.apccontrol.basic.constant.VersionConstant.V5_240802_0;

/**
 * 照明控制器
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@RestController("localLightingController")
@RequestMapping(value = "/lighting")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LightingController {

    private final LightingService lightingService;

    @Operation(summary = "设置-手自动状态", tags = V5_2404_2)
    @PostMapping("/setOperatingParameters")
    public R<Boolean> setOperatingParameters(@RequestBody @Valid LightingOperatingParametersSetParam param) {
        return lightingService.setOperatingParameters(param);
    }

    @Operation(summary = "设置-手自动状态（照明专用）空间详情侧边栏得", tags = V5_240802_0)
    @PostMapping("/setOperatingParametersReturnInfo")
    public R<SetOperatingParametersVO> setOperatingParametersReturnInfo(
            @RequestBody @Valid LightingOperatingParametersSetParam param) {
        return R.success(lightingService.setOperatingParametersReturnInfo(param));
    }

    @Operation(summary = "开关设定，如果有自动先下发手动再下发开关指令 (照明专用)列表", tags = V5_240802_0)
    @PostMapping("/changeManualAutoStatusAndEquipSwitchSetByObjectIds")
    public R<SetOperatingParametersVO> changeManualAutoStatusAndEquipSwitchSetByObjectIds(
            @RequestHeader String projectId,
            @RequestBody @Valid RunningModeLightingDTO param) {
        param.setProjectId(projectId);
        return R.success(lightingService.changeManualAutoStatusAndEquipSwitchSetByObjectIds(param));
    }

    @Operation(summary = "分页查询-照明回路详情列表", tags = V5_2404_2)
    @PostMapping("/pageCircuitDetails")
    public R<LightingDataDto> pageCircuitDetails(@RequestBody @Valid LightingControllerWithCircuitQueryParam param) {
        return R.success(lightingService.pageCircuitDetails(param));
    }

    @Operation(summary = "分页查询-照明控制模块详情列表", tags = V5_2404_2)
    @PostMapping("/pageControllerDetails")
    public R<LightingDataDto> pageControllerDetails(@RequestBody @Valid LightingControllerWithCircuitQueryParam param) {
        return R.success(lightingService.pageControllerDetails(param));
    }
}
