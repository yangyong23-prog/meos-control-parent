package com.persagy.apccontrol.basic.model.dto.lighting;

import com.persagy.apccontrol.basic.model.enumtype.lighting.LightingAbnormalStatusTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.lighting.LightingInterventionTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingTypeSettingModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 照明-标签DTO
 *
 * @author JiYinchuan
 * @since 5.240501.0
 */
@Data
@Schema(title = "照明-标签DTO")
public class LightingBaseTagDTO {

    @Schema(description = "开关状态")
    private DemandLightingTypeSettingModeEnum openCloseStatus;

    @Schema(description = "异常状态")
    private List<LightingAbnormalStatusTypeEnum> abnormalStatuses;

    @Schema(description = "干预类型")
    private List<LightingInterventionTypeEnum> interventionTypes;


    @Schema(description = "需求是否为反馈调节")
    private Boolean demandFeedbackAdjustment;

}
