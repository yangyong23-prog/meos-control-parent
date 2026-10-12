package com.persagy.apccontrol.common.logtrace.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("查询模板参数数据")
public class LogQueryParamDataDto {

    @ApiModelProperty("节点node")
    private String nodecode;

    @ApiModelProperty("节点key")
    private String key ;

    @ApiModelProperty("信息所属节点id")
    private String name;

    @ApiModelProperty("信息所属节点id")
    private int type;

    @ApiModelProperty("信息所属节点id")
    private String value;

}
