package com.persagy.apccontrol.common.logtrace.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("LogInfoExceptionVo")
public class LogInfoExceptionVo {

    @ApiModelProperty("业务对象id")
    private String businessId;

    @ApiModelProperty("无法查询到下级业务对象id对应的信息业务对象列表")
    private List<String> businessChildIds;

}
