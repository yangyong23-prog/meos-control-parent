package com.persagy.apccontrol.basic.service.demand.airconditioning.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class ValueSelectQueryParamByPage {

    @Schema(name = "项目id")
    @NotBlank(message = "{key_d4be1b60bd32403287b0a47366af7092}")
    private String projectId;

    @Schema(name = "开始时间")
    @NotNull(message = "{key_8ceaf15b76524876a9b10eea2e267297}")
    private LocalDateTime startTime;

    @Schema(name = "结束时间")
    @NotNull(message = "{key_3313241b85ef48d9a3d2c43cd5e7930d}")
    private LocalDateTime endTime;

    @Schema(name = "页码")
    @NotNull(message = "{key_e81fab70967c4213bb4f88bb0b57ef02}")
    private Integer pageNumber;

    @Schema(name = "每页条数")
    @NotNull(message = "{key_0ef36dc530554230b031ee4f6d775d9e}")
    private Integer pageSize;
}
