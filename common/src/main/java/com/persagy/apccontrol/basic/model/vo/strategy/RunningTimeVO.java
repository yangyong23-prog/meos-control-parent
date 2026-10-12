package com.persagy.apccontrol.basic.model.vo.strategy;

import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 运行时间VO
 *
 * @author JiYinchuan
 * @since v5.8.0
 */
@Data
@Accessors(chain = true)
@Schema(title = "运行时间VO")
public class RunningTimeVO {

    @Schema(description = "时间")
    private LocalDateTime dateTime;

    @Schema(description = "运行状态")
    private RunStatusEnum runStatus;

    @Schema(description = "运行类型")
    private RunningLogTypeEnum runType;

}
