package com.persagy.apccontrol.basic.model.param.airterminal;

import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 空调末端-空间带状态查询参数
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@Schema(title = "空调末端-空间带状态查询参数")
public class AirTerminalSpaceWithStatusQueryParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_4a7e14edef4d45f5a3c128903799b601}")
    private String projectId;

    @Schema(description = "实例类型")
    @NotNull(message = "{key_3b13463321154c7695bc97753eeede5e}")
    private AirTerminalInstanceTypeEnum instanceType;

    @Schema(description = "实例ID")
    @NotBlank(message = "{key_e8dc2fefc0e244ff95cc77a3352c1826}")
    private String instanceId;
}
