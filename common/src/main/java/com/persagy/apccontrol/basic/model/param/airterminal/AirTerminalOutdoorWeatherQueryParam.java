package com.persagy.apccontrol.basic.model.param.airterminal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 空调末端-室外天气查询参数
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@Schema(title = "空调末端-室外天气查询参数")
public class AirTerminalOutdoorWeatherQueryParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_4a7e14edef4d45f5a3c128903799b601}")
    private String projectId;

}
