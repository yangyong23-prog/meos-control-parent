package com.persagy.apccontrol.basic.model.param.intervention;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 干预任务手动控制参数
 *
 * @author JiYinchuan
 * @since 7.260101.0
 */
@Data
public class InterventionTaskManualControlParam {

    /**
     * 运行监测类型
     */
    @NotNull(message = "{key_a2aa6d0437464447b95289dc0cf17ac9}")
    private RunningMonitorTypeEnum runningMonitorType;

    /**
     * 运行监测二级类型
     */
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    /**
     * 干预任务ID
     */
    @NotNull(message = "Intervention task id can not be null")
    private Long taskId;

    /**
     * 手动控制结束时间，为空时表示提前结束
     */
    private LocalDateTime controlEndTime;

}
