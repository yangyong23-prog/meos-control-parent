package com.persagy.apccontrol.basic.model.param.demand.lighting;

import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.circuit.DtpRwdCircuitFuncTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.circuit.DtpRwdCircuitLampFuncTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 模板下回路预览
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/3/26 18:23
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(title = "照明需求管理-操作-清除配置")
public class DemandLightingCircuitViewParam extends DemandLightingTemplateIdParam {

    @Schema(description = "场景ID")
    private Long sceneId;

    @Schema(description = "照明回路状态")
    private DemandLightingCircuitSettingModeEnum settingMode;

    @Schema(description = "照明回路功能类型")
    private DtpRwdCircuitFuncTypeEnum funcType;

    @Schema(description = "照明回路灯具功能类型")
    private DtpRwdCircuitLampFuncTypeEnum lampFuncType;

    @Schema(description = "当前页码")
    private Integer current;

    @Schema(description = "每页条数")
    private Integer size;

}
