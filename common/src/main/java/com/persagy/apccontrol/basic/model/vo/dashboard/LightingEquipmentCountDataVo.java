package com.persagy.apccontrol.basic.model.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author : wangchenghong
 * @date : 2024/12/3 15:46
 */
@Data
@Schema(description = "Dashboard设备统计")
public class LightingEquipmentCountDataVo {

    @Schema(description = "照明回路统计")
    private CircuitStatisticsVo circuitStatistics;

    @Schema(description = "控制模块统计")
    private ControllerStatisticsVo controllerStatistics;

}
