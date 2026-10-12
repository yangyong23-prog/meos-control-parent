package com.persagy.apccontrol.basic.model.vo.external.demand;

import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
@Schema(title = "查询-照明需求配置结果对外VO")
public class ExternalDemandConfigResultLightingVO extends ExternalDemandConfigResultVO {
    
    @Schema(description = "照明回路ID")
    private String circuitId;
    
    @Schema(description = "配置")
    private DemandLightingCircuitSettingModeEnum settingMode;
}
