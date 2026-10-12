package com.persagy.apccontrol.basic.model.param.security;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 安防Dashboard查询接口
 *
 * @author JiYinchuan
 * @since 5.241201.0
 */
@Data
public class SecurityDashboardQueryParam {

    @Schema(description = "实例id,为空时查询所有")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

}
