package com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning;

import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 空调需求配置--空间工况
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
public class DemandConfigAirconditioningWorkConditionValueObject implements ValueObject {

    @Schema(description = "供冷工况")
    private List<DemandConfigAirconditioningWorkInstanceValueObject> coolingSeason;

    @Schema(description = "供暖工况")
    private List<DemandConfigAirconditioningWorkInstanceValueObject> heatingSeason;

}
