package com.persagy.apccontrol.basic.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 气象监测站视图对象
 *
 * @since 7.260501.0
 */
@Schema(title = "气象监测站VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherStationVO {

    @Schema(description = "设备ID")
    private String id;

    @Schema(description = "设备名称")
    private String name;
}
