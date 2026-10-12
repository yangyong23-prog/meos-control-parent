package com.persagy.apccontrol.basic.resource.param.runningLog;

import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 查询日志分类
 */
@Data
public class QueryLogCategoryParam {
    
    @Schema(name = "一级日志分类")
    @NotNull(message = "{key_1021cb717c2549a085df421fc85a2c24}")
    private String firstLevelCategory;
    
    @Schema(description = "数据开始时间")
    private LocalDateTime dataStartTime;
    
    @Schema(description = "数据结束时间")
    private LocalDateTime dataEndTime;
    
    @Schema(name = "日志标识")
    private RunningLogTypeEnum type;
}
