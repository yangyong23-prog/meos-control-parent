package com.persagy.apccontrol.basic.model.vo.demand.lighting;

import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingTypeSettingModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 需求管理-照明管控对象VO
 *
 * @author yuyao
 */
@Data
@Schema(description = "需求管理-空间回路统计对象")
public class DemandLightingSpaceCircuitVO {

    @Schema(description = "空间id")
    private String spaceId;

    @Schema(description = "照明回路配置模式")
    private DemandLightingTypeSettingModeEnum circuitSettingMode;


}
