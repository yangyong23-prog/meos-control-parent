package com.persagy.apccontrol.basic.model.vo.intervention;


import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionTaskInstanceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 干预配置-配置VO
 */
@Data
public class InterventionTaskConfigVO {
    
    @Schema(description = "任务id")
    private Long taskId;
    
    @Schema(description = "任务名称")
    private String taskName;
    
    @Schema(description = "一级监测类型")
    private RunningMonitorTypeEnum runningMonitorType;
    
    @Schema(description = "运行监测二级类型")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;
    
    @Schema(description = "配置对象")
    private InterventionTaskInstanceDTO configObject;
    
    @Schema(description = "实例对象集合")
    private List<DtpRwdObjectDTO> instance;
    
    @Schema(description = "具体的干预配置")
    private JsonNode configData;
    
    @Schema(description = "干预原因")
    private String reason;
    
    @Schema(description = "开始生效时间")
    private LocalDateTime beginTime;
    
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
    
    @Schema(description = "创建时间")
    protected LocalDateTime createTime;

    
}
