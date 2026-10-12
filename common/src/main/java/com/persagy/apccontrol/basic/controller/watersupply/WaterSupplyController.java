package com.persagy.apccontrol.basic.controller.watersupply;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.watersupply.WaterSupplyModelViewQueryParam;
import com.persagy.apccontrol.basic.model.vo.instance.EquipmentVO;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorWaterSupplyStrategy;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 生活给水控制器
 *
 * @author JiYinchuan
 * @since 7.251202.0
 */
@RestController
@RequestMapping("/waterSupply")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class WaterSupplyController {

    private final RunningMonitorWaterSupplyStrategy runningMonitorWaterSupplyStrategy;

    @Operation(summary = "[查询] 查询模型图设备", tags = VersionConstant.V7_251202_0)
    @PostMapping("/listEquipmentsToModuleView/{type}")
    public R<List<EquipmentVO>> listEquipmentsToModuleView(
            @RequestBody @Valid WaterSupplyModelViewQueryParam param, @PathVariable String type) {
        return R.success(runningMonitorWaterSupplyStrategy.listEquipmentsToModuleView(param, type));
    }
}
