package com.persagy.apccontrol.basic.model.vo.lighting;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInheritModeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;


@Data
@ToString
@Schema(title = "查询-照明回路配置结果")
public class LightingCircuitConfigResultVO {
    
    @Schema(description = "照明回路ID")
    private String circuitId;
    
    @Schema(description = "回路配置")
    private DemandLightingCircuitSettingModeEnum settingMode;
    
    @Schema(description = "配置来源对象名称")
    private String configSourceObjName;
    
    @Schema(description = "配置来源对象ID")
    private String configSourceObjId;
    
    @Schema(description = "配置来源类型")
    private DemandInheritModeEnum configSourceType;
    
}
