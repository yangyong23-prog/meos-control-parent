package com.persagy.apccontrol.basic.model.vo.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 夜景照明状态VO
 *
 * @author JiYinchuan
 * @since 5.240501.0
 */
@Data
@Schema(title = "夜景照明状态VO")
public class LightingNightWithStatusVO {

    @Schema(description = "灯具类型带状态VO")
    private List<LightingLampFuncTypeWithStatusVO> lampFuncTypes;

    @Schema(description = "楼层带状态VO")
    private List<LightingFloorWithStatusVO> floors;

}
