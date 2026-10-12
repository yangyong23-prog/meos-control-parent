package com.persagy.apccontrol.basic.model.param.intervention;


import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class InterventionInfoQueryParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "运行监测类型")
    @NotNull(message = "{key_a2aa6d0437464447b95289dc0cf17ac9}")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    @Schema(description = "运行监测类型")
    @NotNull(message = "{key_a2aa6d0437464447b95289dc0cf17ac9}")
    private DemandInstanceTypeEnum instanceType;

    @Schema(description = "指定查询的实例id")
    @NotEmpty(message = "{key_db46294d82c44d7aa47bd996f27c6154}")
    private Set<String> instanceIds;


    @Schema(description = "干预起始开始时间")
    @NotEmpty(message = "{key_a54af51ee9744b65b7b8cc3d27af6f55}")
    private LocalDateTime beginDateTime;

    @Schema(description = "干预结束时间")
    @NotEmpty(message = "{key_376fd59fb5b64940b3ffa45ba7f388a8}")
    private LocalDateTime endDateTime;

}
