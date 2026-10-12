package com.persagy.apccontrol.basic.model.vo.intervention;

import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class InterventionQueryListVo<T extends InterventionConfigData> {

    @ApiModelProperty(value = "实例id")
    private String instanceId;

    @ApiModelProperty(value = "实例类型")
    private InstanceTypeEnum instanceType;

    @ApiModelProperty(value = "干预配置列表")
    private List<InterventionConfigVo<T>> configList;

}
