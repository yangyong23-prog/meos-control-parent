package com.persagy.apccontrol.common.logtrace.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
@ApiModel("LogNodeInfoVo")
public class LogNodeInfoVo {

    @ApiModelProperty(value = "业务编码")
    private String  businessCode;


    @ApiModelProperty(value = "数据节点id")
    private String  nodeId;


    @ApiModelProperty(value = "业务对象id")
    private String businessId;

    @ApiModelProperty(value = "业务对象关联的下级业务对象id列表")
    private List<String> businessChildIds;

    @ApiModelProperty(value = "业务对象名")
    private String businessDesc;

    @ApiModelProperty(value = "业务对象名", required = true)
    private String data;

    private LocalDateTime createTime;
}
