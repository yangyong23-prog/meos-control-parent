package com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

/**
 * 空调需求配置--环境品质
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
@ToString
@FieldNameConstants
public class DemandConfigAirconditioningEnvironmentQualityObject {

    @Schema(description = "是否参与调度")
    private Boolean isParticipateInScheduling;

    @Schema(description = "是否启用湿度品质配置")
    private Boolean isRhQualityEnabled;

    @Schema(description = "是否启用压差品质配置")
    private Boolean isPressureDiffQualityEnabled;

    @Schema(description = "温度品质配置")
    private DemandConfigAirconditioningTdbQualityValueObject tdbQuality;

    @Schema(description = "二氧化碳品质配置")
    private DemandConfigAirconditioningCO2QualityValueObject cO2Quality;

    @Schema(description = "湿度品质配置")
    private DemandConfigAirconditioningRhQualityValueObject rhQuality;

    @Schema(description = "压差品质配置")
    private DemandConfigAirconditioningPressureDiffQualityValueObject pressureDiffQuality;

}
