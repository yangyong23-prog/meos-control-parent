package com.persagy.apccontrol.basic.model.dto.lighting;

import com.persagy.apccontrol.basic.model.enumtype.lighting.LightingRunningDeviationTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

/**
 * 照明-灯具功能类型标签DTO
 *
 * @author JiYinchuan
 * @since 5.240501.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "照明-灯具功能类型标签DTO")
public class LightingLampFuncTypeTagDTO extends LightingBaseTagDTO {

    @Schema(description = "灯具功能类型")
    private String lampFuncType;

    @Schema(description = "运行偏差类型")
    private Set<LightingRunningDeviationTypeEnum> runningDeviationTypes;

}
