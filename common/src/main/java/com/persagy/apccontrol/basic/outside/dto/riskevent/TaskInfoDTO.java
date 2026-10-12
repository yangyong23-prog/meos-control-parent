package com.persagy.apccontrol.basic.outside.dto.riskevent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/17 14:33
 */
@Data
@ToString
public class TaskInfoDTO {

    @Schema(description = "对象实例id")
    private String objectInstanceId;

    @Schema(description = "任务编码")
    private String taskCode;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;

    @Schema(description = "最后执行人")
    private String lastUpdateBy;

}
