package com.persagy.apccontrol.common.logtrace.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogNodeExtendVo {

    @ApiModelProperty("业务对象id")
    private Long id;

    @ApiModelProperty("信息所属节点id")
    private String nodeId;

    @ApiModelProperty("信息所属节点code")
    private String nodeCode;

    @ApiModelProperty("信息所属code")
    private String key;

    @ApiModelProperty("信息所属名字")
    private String name;

    @ApiModelProperty("信息值")
    private String value;

    @ApiModelProperty("创建信息")
    private LocalDateTime createTime;
}
