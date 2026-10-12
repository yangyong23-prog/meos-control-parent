package com.persagy.apccontrol.basic.model.param.ventilation;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class VentilationModelViewParam {

    @Schema(description = "实例id,为空时查询所有")
    private String instanceId;

    @Schema(description = "实例类型,为空时查询所有",defaultValue = "PROJECT")
    private DemandInstanceTypeEnum instanceType;

    @NotNull(message = "{key_b486266f8a7d4adf9dc807b3f1468887}")
    @Schema(description = "通风类型")
    private RunningMonitorSecondTypeEnum ventilationType;
}
