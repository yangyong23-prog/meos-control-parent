package com.persagy.apccontrol.basic.model.param.parking;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ParkingSpaceStatisticsQueryParam {

    @Schema(description = "实例id,为空时查询所有")
    @NotBlank(message = "{key_1d5fef239fd54a93bcf51c4dbc88ca4b}")
    private String instanceId;

    @Schema(description = "实例类型,为空时查询所有", defaultValue = "PROJECT")
    @NotNull(message = "{key_3b13463321154c7695bc97753eeede5e}")
    private DemandInstanceTypeEnum instanceType;
}
