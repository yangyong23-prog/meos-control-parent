package com.persagy.apccontrol.basic.model.param.cacherwd;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jspecify.annotations.NonNull;

import javax.validation.constraints.NotNull;

/**
 *
 * @description:
 * @author: wxf
 * @create: 2026-03-17 17:16
 **/
@Data
public class CacheRwdBuildParam {
    @Schema(description = "运行监测一级类型")
    private RunningMonitorTypeEnum runningMonitorType;
}