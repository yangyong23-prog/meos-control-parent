package com.persagy.apccontrol.basic.model.param.instance;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.instance.InstanceConfigTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jspecify.annotations.NonNull;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class InstanceConfigQueryParam {

    @Schema(description = "配置类型")
    @NotNull(message = "{key_ab1e8b7a618540a1afa65dc57a12cce6}")
    private @NonNull InstanceConfigTypeEnum configType;

    @Schema(description = "对象类型")
    private DtpRwdObjectTypeEnum objectType;

    @Schema(description = "运行监测二级类型")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    @Schema(description = "运行监测一级类型")
    @NotNull(message = "{key_261245b8e14a4b48b25eb41878934c8b}")
    private @NonNull RunningMonitorTypeEnum runningMonitorType;
    
    @Schema(description = "关联信息")
    private String templateTypeKey;
    
    @Schema(description = "模版 ID")
    private Long templateTypeId;

    @Schema(description = "服务空间ID")
    private Set<String> serviceSpaceIds;

    @Schema(description = "所在空间ID")
    private Set<String> inSpaceIds;

}
