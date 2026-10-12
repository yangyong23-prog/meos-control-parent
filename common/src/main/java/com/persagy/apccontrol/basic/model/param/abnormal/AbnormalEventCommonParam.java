package com.persagy.apccontrol.basic.model.param.abnormal;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel2;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.Collection;

/**
 * 按系统划分的异常事件入参
 *
 * @author yang
 * @version 1.0.0
 * @date 2024/8/23 14:23
 * @since 5.240802.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "按系统划分的异常事件入参")
public class AbnormalEventCommonParam {

    @Schema(description = "实例id,为空时查询所有")
    private String instanceId;

    @Schema(description = "实例类型")
    @NonNull
    private DemandInstanceTypeEnum instanceType;

    @Schema(description = "运行监测类型")
    @NonNull
    private RunningMonitorTypeEnum runningMonitorType;

    @Schema(description = "实例ID集合")
    private Collection<String> objectIdList;

    @Schema(description = "二级级管控目标集合")
    private Collection<RiskMeasureAimLevel2> riskMeasureAimLevel2;

}
