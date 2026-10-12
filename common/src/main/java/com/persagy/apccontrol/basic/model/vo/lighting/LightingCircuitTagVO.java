package com.persagy.apccontrol.basic.model.vo.lighting;

import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingCircuitFeedbackAdjustSettingDTO;
import com.persagy.apccontrol.basic.model.enumtype.lighting.LightingRunningDeviationTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * 照明-照明回路标签VO
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Data
@Schema(title = "照明-照明回路标签VO")
public class LightingCircuitTagVO {

    @Nullable
    @Schema(description = "运行偏差类型")
    private LightingRunningDeviationTypeEnum runningDeviationType;

    @Nullable
    @Schema(description = "反馈调节配置")
    private DemandLightingCircuitFeedbackAdjustSettingDTO feedbackAdjustSettingConfig;

}
