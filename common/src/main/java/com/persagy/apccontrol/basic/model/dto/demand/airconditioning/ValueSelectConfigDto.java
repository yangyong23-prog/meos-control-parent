package com.persagy.apccontrol.basic.model.dto.demand.airconditioning;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ValueSelectConfigDto {

    @Schema(name = "区间最大值")
    private BigDecimal intervalMax;

    @Schema(name = "区间最小值")
    private BigDecimal intervalMin;

    @Schema(name = "温度满足率")
    @NotNull(message = "{key_4045ed6f707e4748ac8b3386c8c24bef}")
    private BigDecimal tempFillRate;

    @Schema(name = "CO2满足率")
    @NotNull(message = "{key_551af6fc1b4e4342919f41c5320483eb}")
    private BigDecimal co2FillRate;
}
