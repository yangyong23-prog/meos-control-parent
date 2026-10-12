package com.persagy.apccontrol.basic.model.vo.demand.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 照明回路对象
 *
 * @author yuyao
 */
@Data
@Schema(description = "需求管理-照明回路对象配置统计")
public class CircuitObjCfgStatisticsVO {

    @Schema(description = "回路总树")
    private Integer totality;

    @Schema(description = "开启总数")
    private Integer openTotal;

    @Schema(description = "关闭总数")
    private Integer closeTotal;

    @Schema(description = "反馈调节总数")
    private Integer feedbackAdjustmentTotal;

    @Schema(description = "未配置总数")
    private Integer notCfgTotal;

}
