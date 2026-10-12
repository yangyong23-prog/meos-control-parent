package com.persagy.apccontrol.basic.service.demand.airconditioning.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ValueSelectElecQueryParam {

    @Schema(name = "项目id")
    @NotNull(message = "{key_d4be1b60bd32403287b0a47366af7092}")
    private String projectId;

    @Schema(name = "开始时间")
    @NotNull(message = "{key_8ceaf15b76524876a9b10eea2e267297}")
    private LocalDateTime startTime;

    @Schema(name = "结束时间")
    @NotNull(message = "{key_3313241b85ef48d9a3d2c43cd5e7930d}")
    private LocalDateTime endTime;

    @Schema(name = "样本开始时间")
    private LocalTime sampleStartTime;

    @Schema(name = "样本结束时间")
    private LocalTime sampleEndTime;

    @Schema(name = "是否需要样本数据，默认需要")
    private Boolean enableSample;

}
