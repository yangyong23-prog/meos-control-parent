package com.persagy.apccontrol.basic.model.dto.demand.lighting;

import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * 需求管理-照明需求-配置结果DTO
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Schema(title = "需求配置-照明需求-配置数据")
public class DemandConfigLightingResultDataDTO {

    @Schema(description = "照明回路配置模式")
    @NotNull(message = "{key_e59ff1c22edb44158ece1b081c425861}")
    private DemandLightingCircuitSettingModeEnum circuitSettingMode;

    @Schema(description = "回路级反馈条件配置")
    private DemandLightingCircuitFeedbackAdjustSettingDTO feedbackAdjustSettings;

    /**
     * 新建当前实例 默认未配置
     *
     * @return 当前实例
     * @since 24Q1 T3S1-5.17.0
     */
    public static DemandConfigLightingResultDataDTO newInstance() {
        return new DemandConfigLightingResultDataDTO().setCircuitSettingMode(
                DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED);
    }
}
