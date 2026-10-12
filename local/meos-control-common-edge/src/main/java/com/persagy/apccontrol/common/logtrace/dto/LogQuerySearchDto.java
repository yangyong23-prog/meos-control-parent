package com.persagy.apccontrol.common.logtrace.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;


@ApiModel("获取日志检索模板搜索dto")
@Data
public class LogQuerySearchDto {

    @ApiModelProperty(value = "模板id", required = true)
    @NotEmpty(message = "templateId is null")
    private Long templateId;


    @ApiModelProperty(value = "数据开始查询时间", required = true)
    @NotEmpty(message = "beginTime is null")
    private LocalDateTime beginTime;

    @ApiModelProperty(value = "数据查询结束时间", required = true)
    @NotEmpty(message = "endTime is null")
    private LocalDateTime endTime;

//    @ApiModelProperty(value = "业务编码", required = true)
//    private Map<String,String> param;

}
