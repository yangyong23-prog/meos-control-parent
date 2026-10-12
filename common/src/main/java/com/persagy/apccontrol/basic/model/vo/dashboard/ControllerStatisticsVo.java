package com.persagy.apccontrol.basic.model.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author : wangchenghong
 * @date : 2024/12/3 16:36
 */
@Data
@Schema(description = "Dashboard设备统计-控制模块统计")
public class ControllerStatisticsVo {

    @Schema(description = "控制模块总数")
    private Integer totalControllerCount;

    @Schema(description = "离线数量")
    private Integer offlineCount;

}
