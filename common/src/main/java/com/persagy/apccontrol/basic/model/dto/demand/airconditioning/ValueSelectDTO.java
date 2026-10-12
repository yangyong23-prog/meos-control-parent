package com.persagy.apccontrol.basic.model.dto.demand.airconditioning;

import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValueSelectDTO {

    @Schema(description = "价值选择类型")
    private BasicsConfigTypeEnum valueSelectType;

    @Schema(description = "价值选择区间配置")
    private List<ValueSelectConfigDto> valueSelectRange;
}
