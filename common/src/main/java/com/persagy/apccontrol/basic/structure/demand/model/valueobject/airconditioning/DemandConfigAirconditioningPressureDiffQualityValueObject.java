package com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning;

import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 空调需求配置-压差品质对象
 *
 * @author JiYinchuan
 * @since 7.260502.0
 */
@Data
public class DemandConfigAirconditioningPressureDiffQualityValueObject implements ValueObject {

    @Schema(description = "是否启用压差品质配置")
    private Boolean isPressureDiffQualityEnabled;

    @Schema(description = "压差下限值")
    private BigDecimal lowerLimitValue;

    @Schema(description = "压差上限值")
    private BigDecimal upperLimitValue;

}
