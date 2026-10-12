package com.persagy.apccontrol.basic.model.param.intervention;


import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 干预任务配置查询参数
 */
@Data
public class InterventionTaskConfigQueryParam {
    
    @Schema(description = "运行监测类型")
    @NotNull(message = "{key_a2aa6d0437464447b95289dc0cf17ac9}")
    private RunningMonitorTypeEnum runningMonitorType;
    
    @Schema(description = "运行监测二级类型")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;
    
    @Schema(description = "实例 ID列表")
    private Set<String> instanceIds;
    
    @Schema(description = "实例类型")
    private DemandInstanceTypeEnum instanceType;
    
    @Schema(description = "开始生效时间")
    @NotNull(message = "{key_6bde0cf5526c42b49cbfd74f79b489dd}")
    private LocalDateTime beginTime;
    
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
