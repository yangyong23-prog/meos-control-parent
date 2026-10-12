package com.persagy.apccontrol.controller.airconditioning;

import com.persagy.apccontrol.basic.model.param.airconditioning.ForceControlStatusEditParam;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorAirConditioningColdSourceStrategy;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 空调冷源控制器
 */
@RestController("localAirConditioningController")
@RequestMapping("/airconditioning")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AirConditioningController {

    private final RunningMonitorAirConditioningColdSourceStrategy runningMonitorAirConditioningColdSourceStrategy;

    @Operation(summary = "[编辑] 强制控制状态")
    @PostMapping("/editForceControlStatus")
    public R<Boolean> editForceControlStatus(@RequestBody @Valid ForceControlStatusEditParam param) {
        return R.success(runningMonitorAirConditioningColdSourceStrategy.editForceControlStatus(param));
    }
}
