package com.persagy.apccontrol.basic.model.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author : wangchenghong
 * @date : 2024/12/3 16:36
 */
@Data
@Schema(description = "Dashboard设备统计-照明回路统计")
public class CircuitStatisticsVo {

    @Schema(description = "照明回路总数")
    private Integer totalCircuitCount;

    @Schema(description = "离线数量")
    private Integer offlineCount;

    @Schema(description = "控反不一致数量")
    private Integer orderFailAlarmCount;

    @Schema(description = "自动数量")
    private Integer autoCount;

    @Schema(description = "手动数量")
    private Integer manualCount;

}
