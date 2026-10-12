package com.persagy.apccontrol.common.logtrace.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
@Data
@ApiModel("添加日志")
public class LogNodeExtendDto {


    @ApiModelProperty("信息所属")
    private String extendCode;

    @ApiModelProperty("信息所属名字")
    private String extendName;

    @ApiModelProperty("信息值")
    private String extendValue;

}
