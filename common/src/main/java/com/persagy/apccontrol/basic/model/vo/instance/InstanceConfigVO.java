package com.persagy.apccontrol.basic.model.vo.instance;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.instance.InstanceConfigTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class InstanceConfigVO {

    @Schema(description = "配置类型")
    private InstanceConfigTypeEnum configType;

    @Schema(description = "对象类型")
    private DtpRwdObjectTypeEnum objectType;

    @Schema(description = "运行监测二级类型")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    @Schema(description = "运行监测一级类型")
    private RunningMonitorTypeEnum runningMonitorType;

    @Schema(description = "配置数据")
    private JsonNode configData;

    @Schema(description = "项目ID")
    private String projectId;
}
