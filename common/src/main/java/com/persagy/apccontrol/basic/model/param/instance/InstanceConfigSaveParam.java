package com.persagy.apccontrol.basic.model.param.instance;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.instance.InstanceConfigTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class InstanceConfigSaveParam {

    @Schema(description = "配置类型")
    @NotNull(message = "{key_ab1e8b7a618540a1afa65dc57a12cce6}")
    private @NonNull InstanceConfigTypeEnum configType;

    @Schema(description = "对象类型")
    @NotNull(message = "{key_86c480967d6542b0820033cce366d29a}")
    private @NonNull DtpRwdObjectTypeEnum objectType;

    @Schema(description = "运行监测二级类型")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    @Schema(description = "运行监测一级类型")
    @NotNull(message = "{key_261245b8e14a4b48b25eb41878934c8b}")
    private @NonNull RunningMonitorTypeEnum runningMonitorType;

    @Schema(description = "配置数据")
    private JsonNode configData;
    
    @Schema(description = "关联信息")
    private String relMsg;
}
