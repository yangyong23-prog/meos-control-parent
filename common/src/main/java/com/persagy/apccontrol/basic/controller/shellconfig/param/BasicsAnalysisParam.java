package com.persagy.apccontrol.basic.controller.shellconfig.param;

import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * 基础数据分析参数
 */
@Data
public class BasicsAnalysisParam {

    @Schema(description = "分析类型", required = true)
    @NotNull(message = "{key_219fc427db574632a1f9fa05c619cd89}")
    private RunningMonitorSecondTypeEnum type;

    @Schema(description = "实例ID列表", required = true)
    @NotNull
    private List<String> instanceIds;

    @Schema(description = "实例类型", required = true)
    @NotNull
    private InstanceTypeEnum instanceType;

    @Schema(description = "分析日期", required = true)
    @NotNull
    private LocalDate date;
}
