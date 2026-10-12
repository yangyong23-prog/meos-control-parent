package com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning;

import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 空调需求配置--价值选择
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
public class DemandConfigAirconditioningValueSelectValueObject implements ValueObject {

    @Schema(description = "价值选择类型")
    private BasicsConfigTypeEnum valueSelectType;

    public DemandConfigAirconditioningValueSelectValueObject() {

    }

    public DemandConfigAirconditioningValueSelectValueObject(BasicsConfigTypeEnum valueSelectType) {
        this.valueSelectType = valueSelectType;
    }

}
