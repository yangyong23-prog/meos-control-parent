package com.persagy.apccontrol.basic.controller.airconditioning;

import com.persagy.apccontrol.basic.model.param.airconditioning.ForceControlStatusQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.vo.airconditioning.ForceControlStatusVO;
import com.persagy.apccontrol.basic.model.vo.instance.EquipmentVO;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorAirConditioningColdSourceStrategy;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Set;

/**
 * 空调冷源控制器
 */
@RestController
@RequestMapping(value = {"/airconditioning", "/hot", "/ACAI"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AirConditioningController {

    private final RunningMonitorAirConditioningColdSourceStrategy runningMonitorAirConditioningColdSourceStrategy;

    @Operation(summary = "[查询] 强制控制状态")
    @PostMapping("/getForceControlStatus")
    public R<ForceControlStatusVO> getForceControlStatus(@RequestBody @Valid ForceControlStatusQueryParam param) {
        return R.success(runningMonitorAirConditioningColdSourceStrategy.getForceControlStatus(param));
    }

    @Operation(summary = "[查询] 查询模型图设备")
    @PostMapping("/listEquipmentsToModuleView/{type}")
    public R<Set<EquipmentVO>> listEquipmentsToModuleView(@RequestBody @Valid RunningMonitorEquipmentQueryParam param, @PathVariable String type) {
        return R.success(runningMonitorAirConditioningColdSourceStrategy.listEquipmentsToModuleView(param, type));
    }
}
