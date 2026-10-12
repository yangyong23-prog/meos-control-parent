package com.persagy.apccontrol.basic.model.vo.demand.lighting;

import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingCircuitFeedbackAdjustSettingDTO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingTypeSettingModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.lang.Nullable;

/**
 * 需求管理-照明需求-当前场景模式
 *
 * @author zhangqiankun
 * @since 24Q1 T3S2-5.18.0
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "需求管理-照明需求-当前场景模式")
public class DemandLightingSceneModel {

    @Schema(description = "场景ID")
    private Long sceneId;

    @Schema(description = "场景名称")
    private String sceneName;

    @Schema(description = "场景模式")
    private DemandLightingTypeSettingModeEnum sceneMode;

    @Schema(description = "回路状态")
    private DemandLightingCircuitSettingModeEnum settingMode;

    @Nullable
    @Schema(description = "回路级反馈调节条件配置")
    private DemandLightingCircuitFeedbackAdjustSettingDTO feedbackAdjustSetting;

}
