package com.persagy.apccontrol.basic.model.dto.demand.lighting;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingFeedbackAdjustTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingTypeSettingModeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.circuit.DtpRwdCircuitFuncTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 需求管理-照明需求-功能类型配置DTO
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Schema(title = "需求管理-照明需求-功能类型配置DTO")
public class DemandLightingFuncTypeSettingDTO implements BaseCheckParamProvider {

    @Schema(description = "照明回路功能类型")
    @NotNull(message = "{key_0b89340dffc8430a9cc7967116246d55}")
    private DtpRwdCircuitFuncTypeEnum funcType;

    @Schema(description = "类型配置模式")
    //@NotNull(message = "类型配置模式不能为空")
    private DemandLightingTypeSettingModeEnum settingMode;

    @Schema(description = "照明回路灯具类型配置 v5.240402.0 废弃该层级")
    @Valid
    @Deprecated
    private List<DemandLightingLampFuncTypeSettingDTO> lampFuncTypeSettings;

    @Schema(description = "照明回路配置")
    @Valid
    private List<DemandLightingCircuitSettingDTO> circuitSettings;

    @Schema(description = "照明回路编组配置")
    @Valid
    private List<DemandLightingCircuitGroupSettingDTO> circuitGroupSettings;

    @Schema(description = "空间反馈调节类型（室内/室外照度调节）")
    private DemandLightingFeedbackAdjustTypeEnum spaceFeedbackAdjustType;

    //@Override
    //public void checkAndSet() {
    //    if (CollectionUtils.isNotEmpty(lampFuncTypeSettings)) {
    //        lampFuncTypeSettings.forEach(DemandLightingLampFuncTypeSettingDTO::checkAndSet);
    //        if (checkRepeat(lampFuncTypeSettings.stream()
    //                .map(DemandLightingLampFuncTypeSettingDTO::getLampFuncType)
    //                .collect(Collectors.toList()))) {
    //            throw new ValidationException("照明回路灯具功能类型重复");
    //        }
    //    }
    //}
}
