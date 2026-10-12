package com.persagy.apccontrol.common.logtrace.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("添加日志")
@Data
public class LogInfoAddDto {

    @ApiModelProperty(value = "业务应用标识", required = true)
    private String application;

    @ApiModelProperty(value = "添加列表", required = true)
    private List<LogInfoDto> logInfos;


}
