package com.persagy.apccontrol.common.logtrace.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel("根据模板和参数查询数据")
public class LogQueryTemplateAndParamDto {

    @ApiModelProperty(value = "模板id", required = true)
    @NotEmpty(message = "templateId is null")
    private Long templateId;

    @ApiModelProperty(value = "参数列表", required = false)
    private List<LogQueryParamDataDto> params;

    @ApiModelProperty(value = "起始时间", required = true)
    private LocalDateTime beginTime;

    @ApiModelProperty(value = "结束时间", required = true)
    private LocalDateTime endTime;

    @ApiParam(value = "页面大小" ,required = true)
    private int size;

    @ApiParam(value = "当前页码" ,required = true)
    private int current;
}
