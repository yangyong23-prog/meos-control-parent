package com.persagy.apccontrol.basic.model.vo.runningstandardboard;

import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.ParticipateInSchedulingEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author : wangchenghong
 * @date : 2024/11/20 11:47
 */
@Data
@Schema(description = "运行标准看板-空调运行标准")
public class AirconditioningRunningStandardObjectVo {

    @Schema(description = "空调季")
    private String seasonType;

    @Schema(description = "空调工况")
    private String seasonCondition;

    @Schema(description = "价值选择类型")
    private BasicsConfigTypeEnum valueSelectType;

    @Schema(description = "是否允许提前关机")
    private Boolean isAllowEarlyShutdown;

    @Schema(description = "是否允许响应调节")
    private Boolean isAllowResponseRegulation;

    @Schema(description = "响应调节时间")
    private List<DemandTimeIntervalValueObject> responseRegulationTimeIntervals;

    @Schema(description = "是否参与调度")
    private ParticipateInSchedulingEnum participateInScheduling;

    @Schema(description = "是否无需求")
    private Boolean noDemand;

    @Schema(description = "是否算法自动计算")
    private Boolean isAlgorithmCompute;

    @Schema(description = "空调需求时间")
    private List<DemandTimeIntervalValueObject> airConditioningDemandTimeIntervals;

    @Schema(description = "温度品质配置")
    private DemandConfigAirconditioningTdbQualityValueObject tdbQuality;

    @Schema(description = "二氧化碳品质配置")
    private DemandConfigAirconditioningCO2QualityValueObject cO2Quality;

    @Schema(description = "湿度品质配置")
    private DemandConfigAirconditioningRhQualityValueObject rhQuality;

    @Schema(description = "压差品质配置")
    private DemandConfigAirconditioningPressureDiffQualityValueObject pressureDiffQuality;

    @Schema(description = "空间运行工况标准配置")
    private DemandConfigAirconditioningWorkConditionValueObject workCondition;

}
