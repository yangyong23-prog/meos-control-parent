package com.persagy.apccontrol.basic.model.dto.intervention;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.persagy.apccontrol.basic.utils.ParameterCheck;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 末端干预的配置对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AirTerminalInterventionTaskConfigDataDTO extends InterventionTaskConfigDataDTO {
    
    @Schema(description = "开关配置")
    private Boolean switchSet;
    
    @Schema(description = "温度目标")
    private BigDecimal tdbTarget;
    
    @Schema(description = "co2目标")
    @JsonProperty("CO2Target")
    private BigDecimal CO2Target;
    
    @Override
    public void check() {
        ParameterCheck.isTrue(switchSet == null, "开关配置不能为空");
    }
}
