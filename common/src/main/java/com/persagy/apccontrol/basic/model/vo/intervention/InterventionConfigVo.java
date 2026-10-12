package com.persagy.apccontrol.basic.model.vo.intervention;

import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.structure.intervention.enumeration.InterventionStatusEnum;
import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(value = "干预配置Vo")
public class InterventionConfigVo<T extends InterventionConfigData> {


    @ApiModelProperty(value = "干预配置id")
    private Long id;

    @ApiModelProperty(value = "被干预的实例id")
    private String instanceId;

    @ApiModelProperty(value = "干预起始时间")
    private LocalDateTime beginTime;

    @ApiModelProperty(value = "干预具体的配置")
    private T configData;

    @ApiModelProperty(value = "结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "干预配置描述")
    private List<InterventionConfigInheritDescVo> inherit;

    @ApiModelProperty(value = "实例类型")
    private InstanceTypeEnum instanceType;


    @ApiModelProperty(value = "干预原因")
    private String reason;

    @ApiModelProperty(value = "干预类型")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    @ApiModelProperty(value = "干预状态", required = true)
    private InterventionStatusEnum status;
}
