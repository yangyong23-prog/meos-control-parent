package com.persagy.apccontrol.basic.model.param.log;

import com.persagy.apccontrol.basic.model.enumtype.runningLog.NowRunningLogCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "页面播报查询参数")
public class NowPageBroadcastQueryParam {
    
    @Schema(name = "一级日志分类")
    private NowRunningLogCategory firstLevelCategory;
    
    @Schema(name = "二级日志分类")
    private String secondLevelCategory;
    
    
}
