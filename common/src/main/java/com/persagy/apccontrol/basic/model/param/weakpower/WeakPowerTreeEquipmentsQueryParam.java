package com.persagy.apccontrol.basic.model.param.weakpower;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 运行监测-弱电设备树查询参数
 */
@Data
@ToString(callSuper = true)
public class WeakPowerTreeEquipmentsQueryParam {


    @Schema(description = "运行监测二级类型")
    @NotNull(message = "{key_4a1af0119a4f4fdc8f6cfe10e1ea451e}")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    @Schema(description = "系统ID")
    @NotBlank(message = "{key_6fc14063bc9143f19673d1d21b6c5e4d}")
    private String systemId;

}
