package com.persagy.apccontrol.common.logtrace.vo;


import com.persagy.apccontrol.common.logtrace.entity.LogGroupTemplateEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@ApiModel("LogQueryTemplateRecordVo")
public class LogQueryTemplateRecordVo {

    @ApiModelProperty(value = "模板表头列表")
    private List<LogGroupTemplateEntity> headers;

    @ApiModelProperty(value = "表格数据")
    List<Map<Long,String>> sheetData;
}
