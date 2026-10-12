package com.persagy.apccontrol.basic.resource.param.runningLog;

import com.persagy.apccontrol.basic.model.enumtype.runningLog.NowRunningLogCategory;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RunningLogEquipmentListQueryParam {

    @Schema(name = "项目id")
    private String projectId;
    
    @Schema(name = "系统id")
    private String systemId;
    
    @Schema(name = "分类列表(二级分类)")
    private List<String> categoryList;
    
    @Schema(name = "系统编码")
    private String systemClassCode;
    
    @Schema(description = "数据开始时间")
    @NotNull(message = "{key_e7c227d536d74208bd9918d6861d4803}")
    private LocalDateTime dataStartTime;
    
    @Schema(description = "数据结束时间")
    @NotNull(message = "{key_7ab9a74713794a648429d5111e95eb39}")
    private LocalDateTime dataEndTime;
    
    @Schema(name = "日志标识")
    private RunningLogTypeEnum type;

    @Schema(name = "关键字")
    private String word;
    
    
    @Schema(name = "新版日志-一级分类")
    private NowRunningLogCategory firstLevelCategory;
    
    @Schema(name = "新版日志-二级分类")
    private String secondLevelCategory;
}
