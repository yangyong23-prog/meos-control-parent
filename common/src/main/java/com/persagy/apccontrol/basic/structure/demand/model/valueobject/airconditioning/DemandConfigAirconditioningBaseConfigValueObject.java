package com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning;

import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 空调需求配置--基础配置
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
public class DemandConfigAirconditioningBaseConfigValueObject implements ValueObject {

    // ------------------------- 空调需求时间配置 -------------------------

    @Schema(description = "是否算法自动计算")
    private Boolean isAlgorithmCompute;

    @Schema(description = "空调需求时间")
    private List<DemandTimeIntervalValueObject> airConditioningDemandTimeIntervals;

    @Schema(description = "是否无需求")
    private Boolean noDemand;

    // ------------------------- 响应调节配置 -------------------------

    @Schema(description = "是否允许提前关机")
    private Boolean isAllowEarlyShutdown;

    @Schema(description = "是否允许响应调节")
    private Boolean isAllowResponseRegulation;

    @Schema(description = "响应调节时间")
    private List<DemandTimeIntervalValueObject> responseRegulationTimeIntervals;

}
