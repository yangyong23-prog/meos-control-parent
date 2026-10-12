package com.persagy.apccontrol.common.logtrace.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;

@ApiModel("日志详细信息")
@Data
public class LogInfoDto {


    @ApiModelProperty(value = "业务编码", required = true)
    @NotEmpty(message = "businessCode is null")
    private String  businessCode;

    @ApiModelProperty(value = "业务对象id", required = true)
    @NotEmpty(message = "businessId is null")
    private String businessId;

    @ApiModelProperty(value = "业务对象关联的下级业务对象id列表")
    private List<String> businessChildIds;

    @ApiModelProperty(value = "业务对象名")
    private String businessDesc;

    @ApiModelProperty(value = "业务对象对应的时间")
    private LocalDateTime businessTime;

    @ApiModelProperty(value = "业务对象名", required = true)
    @NotEmpty(message = "data is null")
    private List<LogNodeExtendDto>  data;
}
