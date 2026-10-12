package com.persagy.apccontrol.basic.model.vo.demand.lighting;

import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingCircuitFeedbackAdjustSettingDTO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.circuit.DtpRwdCircuitFuncTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.circuit.DtpRwdCircuitLampFuncTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * 需求管理-照明需求-照明回路配置VO
 *
 * @author zhangqiankun
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Schema(title = "需求管理-照明需求-照明回路配置VO")
public class DemandLightingCircuitSettingVO {

    @Schema(description = "照明回路ID")
    private String circuitId;

    @Schema(description = "照明回路本地编码")
    private String circuitLocalId;

    @Schema(description = "照明回路本地名称")
    private String circuitLocalName;

    @Schema(description = "服务区域类型")
    private String serviceAreaType;

    @Schema(description = "照明回路功能类型")
    private String funcType;

    @Schema(description = "照明回路功能类型名称")
    private String funcTypeName;

    @Schema(description = "自动控制模式")
    private List<String> autoCtrlType;

    @Schema(description = "灯具类型")
    private String lampFuncType;

    @Schema(description = "灯具功能类型名称")
    private String lampFuncTypeName;

    @Schema(description = "照明回路配置模式")
    private DemandLightingCircuitSettingModeEnum settingMode;

    @Nullable
    @Schema(description = "回路级反馈调节条件配置")
    private DemandLightingCircuitFeedbackAdjustSettingDTO feedbackAdjustSetting;

    @Schema(description = "场景配置模式")
    private List<DemandLightingSceneModel> sceneModes;

    public String getFuncTypeName() {
        if (funcTypeName == null && funcType != null) {
            DtpRwdCircuitFuncTypeEnum analyze = DtpRwdCircuitFuncTypeEnum.analyze(funcType);
            funcTypeName = analyze == null ? null : analyze.getReasonPhrase();
        }
        return funcTypeName;
    }

    public String getLampFuncTypeName() {
        if (lampFuncTypeName == null && lampFuncType != null) {
            DtpRwdCircuitLampFuncTypeEnum analyze = DtpRwdCircuitLampFuncTypeEnum.analyze(lampFuncType);
            lampFuncTypeName = analyze == null ? null : analyze.getReasonPhrase();
        }
        return lampFuncTypeName;
    }

}
