package com.persagy.apccontrol.basic.model.param.intervention;


import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 干预任务配置查询参数
 * 外部查询接口
 */
@Data
public class InterventionTaskConfigOutsideQueryParam {
    
    @Schema(description = "运行监测类型")
    @NotNull(message = "{key_a2aa6d0437464447b95289dc0cf17ac9}")
    private RunningMonitorTypeEnum runningMonitorType;
    
    @Schema(description = "运行监测二级类型")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;
    
    @Schema(description = "查询时间")
    @NotNull(message = "{key_d8c07bea0cca4e50993f203c37a48d63}")
    private LocalDateTime taskTime;
    
}
