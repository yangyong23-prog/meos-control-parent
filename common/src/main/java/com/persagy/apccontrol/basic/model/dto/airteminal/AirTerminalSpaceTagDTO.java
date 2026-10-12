package com.persagy.apccontrol.basic.model.dto.airteminal;

import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalCarbonDioxideIntervalEnum;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalDemandStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalTemperatureIntervalEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * 空调末端-空间标签DTO
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@Schema(title = "空调末端-空间标签DTO")
public class AirTerminalSpaceTagDTO {

    @Schema(description = "是否异常")
    private Boolean abnormal;

    @Nullable
    @Schema(description = "品质-温度区间")
    private AirTerminalTemperatureIntervalEnum temperatureInterval;

    @Nullable
    @Schema(description = "品质-CO₂区间")
    private AirTerminalCarbonDioxideIntervalEnum carbonDioxideInterval;

    @Nullable
    @Schema(description = "需求状态-温度")
    private AirTerminalDemandStatusEnum temperatureDemandStatus;

    @Nullable
    @Schema(description = "需求状态-CO₂")
    private AirTerminalDemandStatusEnum carbonDioxideDemandStatus;

}
