package com.persagy.apccontrol.basic.model.vo.lighting;

import com.persagy.apccontrol.basic.model.dto.lighting.LightingSpaceTagDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.lighting.LightingAbnormalStatusTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.lighting.LightingInterventionTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.lighting.LightingRunningDeviationTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingTypeSettingModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 照明空间带状态VO
 *
 * @author MuAnfu
 * @since 5.240402.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "照明空间带状态VO")
public class LightingSpaceWithStatusVO extends DtpRwdSpaceDTO {

    @Schema(description = "开关状态")
    private DemandLightingTypeSettingModeEnum openCloseStatus;

    @Schema(description = "异常状态")
    private List<LightingAbnormalStatusTypeEnum> abnormalStatuses;

    @Schema(description = "干预类型")
    private List<LightingInterventionTypeEnum> interventionTypes;

    @Schema(description = "运行偏差类型")
    private Set<LightingRunningDeviationTypeEnum> runningDeviationTypes;

    @Schema(description = "需求是否为反馈调节")
    private Boolean demandFeedbackAdjustment;

    @Schema(description = "功能类型编码-照明回路")
    private List<String> funcTypes;

    @Schema(description = "灯具类型编码-照明回路")
    private List<String> lampFuncTypes;

    @Schema(description = "关联控制器")
    private List<DtpRwdObjectDTO> relControllers;

    @Schema(description = "参考面照度")
    private String workPlaneLux;

    /**
     * 合并tag
     *
     * @param vo  vo
     * @param tag 标签
     * @since 5.240402.0
     */
    public static void mergeTag(LightingSpaceWithStatusVO vo, LightingSpaceTagDTO tag) {
        if (Objects.nonNull(tag)) {
            vo.setInterventionTypes(tag.getInterventionTypes());
            vo.setOpenCloseStatus(tag.getOpenCloseStatus());
            vo.setAbnormalStatuses(tag.getAbnormalStatuses());
            vo.setRunningDeviationTypes(tag.getRunningDeviationTypes());
            vo.setDemandFeedbackAdjustment(tag.getDemandFeedbackAdjustment());
            vo.setWorkPlaneLux(tag.getWorkPlaneLux());
        }
    }
}
