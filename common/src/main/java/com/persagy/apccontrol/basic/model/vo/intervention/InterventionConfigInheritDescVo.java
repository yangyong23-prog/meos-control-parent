package com.persagy.apccontrol.basic.model.vo.intervention;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "集成干预配置描述Vo")
public class InterventionConfigInheritDescVo {


    @ApiModelProperty(value = "实例id")
    private String instanceId;

    @ApiModelProperty(value = "实例名字")
    private String instanceName;

    @ApiModelProperty(value = "实例类型")
    private DemandInstanceTypeEnum instanceType;
}
