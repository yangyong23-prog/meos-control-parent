package com.persagy.apccontrol.basic.model.param.abnormal;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel2;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Collection;
import java.util.List;

/**
 * 查询设备状态
 *
 * @author yang
 * @version 1.0.0
 * @date 2024/4/29 14:23
 * @since 5.240402.0
 */
@Data
@Schema(title = "异常事件查询参数")
public class AbnormalEventQueryParam {

    @Schema(description = "运行监测二级类型, 不为空时按类型过滤 机房安全")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    @Schema(description = "实例ID集合")
    @NotEmpty(message = "{key_651db6af0ce94a9e96020ebd8c565be9}")
    private Collection<String> instanceIds;

    @Schema(description = "二级级管控目标集合")
    private List<RiskMeasureAimLevel2> riskMeasureAimLevel2;

    @Schema(description = "运行监测一级类型, 不为空时按类型过滤 dashboard照明/安防")
    private RunningMonitorTypeEnum runningMonitorType;

}
