package com.persagy.apccontrol.basic.model.param.lighting;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 需求目标查询参数
 *
 * @author JiYinchuan
 * @since 7.260101.0
 */
@Data
public class LightingDemandTargetQueryParam {

    /**
     * 运行监测二级类型
     */
    @NotNull(message = "Running monitor second type can not be null")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    /**
     * 需求实例类型
     */
    @NotNull(message = "Instance type can not be null")
    private DemandInstanceTypeEnum instanceType;

    /**
     * 需求实例ID
     */
    @NotBlank(message = "Instance id can not be null")
    private String instanceId;

    /**
     * 需求目标日期, 为空时默认当天
     */
    @Schema(description = "需求目标日期, 为空时默认当天")
    private LocalDate date;

}
