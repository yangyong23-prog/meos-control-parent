package com.persagy.apccontrol.basic.model.dto.demand.lighting;

import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

/**
 * 需求管理-照明需求-照明回路配置DTO
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "需求管理-照明需求-照明回路配置DTO")
public class DemandLightingCircuitSettingDTO {

    @Schema(description = "照明回路ID")
    @NotBlank(message = "{key_d76ea9b42948435aabcf79f837dc128f}")
    private String circuitId;

    @Schema(description = "灯具类型")
    //@NotBlank(message = "灯具类型不能为空")
    private String lampFuncType;

    @Schema(description = "照明回路配置模式")
    //@NotNull(message = "照明回路配置模式不能为空")
    private DemandLightingCircuitSettingModeEnum settingMode;

    @Schema(description = "回路级反馈调节条件配置")
    @Valid
    private DemandLightingCircuitFeedbackAdjustSettingDTO feedbackAdjustSetting;

}
