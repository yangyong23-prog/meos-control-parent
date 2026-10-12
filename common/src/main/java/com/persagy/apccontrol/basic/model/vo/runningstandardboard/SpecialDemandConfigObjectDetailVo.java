package com.persagy.apccontrol.basic.model.vo.runningstandardboard;

import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningCO2QualityValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningPressureDiffQualityValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningRhQualityValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbQualityValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author : wangchenghong
 * @date : 2024/11/20 11:16
 */
@Data
@Schema(description = "运行标准看板-特殊需求配置-详情")
public class SpecialDemandConfigObjectDetailVo {

    @Schema(description = "实例ID")
    private String instanceId;

    @Schema(description = "实例类型")
    private String instanceType;

    @Schema(description = "实例名称")
    private String instanceName;

    @Schema(description = "当前对象归属建筑id")
    private String buildingId;

    @Schema(description = "当前对象归属建筑名称")
    private String buildingName;

    @Schema(description = "当前对象归属楼层id")
    private String floorId;

    @Schema(description = "当前对象归属楼层名称")
    private String floorName;

    @Schema(description = "引用空间详情")
    private ReferenceSpaceVo referenceSpace;

    @Schema(description = "是否无需求")
    private Boolean noDemand;

    @Schema(description = "是否算法自动计算")
    private Boolean isAlgorithmCompute;

    @Schema(description = "空调需求时间")
    private List<DemandTimeIntervalValueObject> airConditioningDemandTimeIntervals;

    @Schema(description = "响应调节时间")
    private List<DemandTimeIntervalValueObject> responseRegulationTimeIntervals;

    @Schema(description = "温度品质配置")
    private DemandConfigAirconditioningTdbQualityValueObject tdbQuality;

    @Schema(description = "二氧化碳品质配置")
    private DemandConfigAirconditioningCO2QualityValueObject cO2Quality;

    @Schema(description = "是否启用湿度品质配置")
    private Boolean isRhQualityEnabled;

    @Schema(description = "湿度品质配置")
    private DemandConfigAirconditioningRhQualityValueObject rhQuality;

    @Schema(description = "是否启用压差品质配置")
    private Boolean isPressureDiffQualityEnabled;

    @Schema(description = "压差品质配置")
    private DemandConfigAirconditioningPressureDiffQualityValueObject pressureDiffQuality;

    @Schema(description = "是否允许响应调节")
    private Boolean isAllowResponseRegulation;

}
