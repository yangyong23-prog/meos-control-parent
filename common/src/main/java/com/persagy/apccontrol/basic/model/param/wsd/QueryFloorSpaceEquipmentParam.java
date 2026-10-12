package com.persagy.apccontrol.basic.model.param.wsd;


import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "空间设备信息查询参数")
public class QueryFloorSpaceEquipmentParam {

    @Schema(description = "实例ID")
    @NotBlank(message = "{key_e8dc2fefc0e244ff95cc77a3352c1826}")
    private String instanceId;

    @Schema(description = "实例类型")
    @NotNull(message = "{key_3b13463321154c7695bc97753eeede5e}")
    private InstanceTypeEnum instanceType;

}
