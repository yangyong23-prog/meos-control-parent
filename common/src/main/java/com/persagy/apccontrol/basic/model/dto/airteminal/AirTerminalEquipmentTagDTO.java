package com.persagy.apccontrol.basic.model.dto.airteminal;

import com.persagy.apccontrol.basic.model.enumtype.OnlineStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalAbnormalStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * 空调末端-设备标签DTO
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@Schema(title = "空调末端-设备标签DTO")
public class AirTerminalEquipmentTagDTO {

    @Nullable
    @Schema(description = "运行状态")
    private RunStatusEnum runStatus;

    @Schema(description = "在线状态")
    private OnlineStatusEnum onlineStatus;

    @Schema(description = "异常状态")
    private List<AirTerminalAbnormalStatusEnum> abnormalStatuses;

}
