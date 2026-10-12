package com.persagy.apccontrol.basic.resource.param.runningLog;

import com.persagy.apccontrol.basic.model.enumtype.runningLog.NowRunningLogCategory;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 新版运行日志查询参数
 */
@Data
public class NowRunningLogQueryParam {
    
    @Schema(name = "一级日志分类")
    @NotNull(message = "{key_1021cb717c2549a085df421fc85a2c24}")
    private NowRunningLogCategory firstLevelCategory;
    
    @Schema(name = "二级日志分类")
    private String secondLevelCategory;
    
    @Schema(name = "查询日期")
    private LocalDateTime workDate;
    
    @Schema(description = "数据开始时间")
    @NotNull(message = "{key_e7c227d536d74208bd9918d6861d4803}")
    private LocalDateTime dataStartTime;
    
    @Schema(description = "数据结束时间")
    @NotNull(message = "{key_7ab9a74713794a648429d5111e95eb39}")
    private LocalDateTime dataEndTime;
    
    @Schema(description = "生效开始时间")
    private LocalDateTime effectStartTime;
    
    @Schema(description = "生效结束时间")
    private LocalDateTime effectEndTime;
    
    @Schema(name = "日志标识")
    private RunningLogTypeEnum type;
    
    @Schema(name = "操作对象id")
    private Set<String> objectIds;
    
    @Schema(name = "系统 ID")
    private String systemId;
    
    @Schema(name = "关键字模糊匹配(操作人/对象名称/操作关键字)")
    private String keyWord;
    
    @NotNull(message = "{key_e81fab70967c4213bb4f88bb0b57ef02}")
    @Min(value = 1)
    private int pageNumber;
    
    @NotNull(message = "{key_0ef36dc530554230b031ee4f6d775d9e}")
    @Min(value = 1)
    private int pageSize;
    
    /**
     * 是否降序, 默认为 {@code true}
     */
    private boolean desc = true;
    
}
