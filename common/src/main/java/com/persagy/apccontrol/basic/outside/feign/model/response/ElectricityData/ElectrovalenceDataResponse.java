package com.persagy.apccontrol.basic.outside.feign.model.response.ElectricityData;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import com.persagy.validate.utility.annotation.NumberToScaleString;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ElectrovalenceDataResponse extends BaseResponse {

    @Schema(name = "日期")
    private LocalDateTime date;

    @Schema(name = "低谷时段")
    private String periodName;

    @Schema(name = "电价")
    @NumberToScaleString
    private BigDecimal electrovalenceData;

    @NotNull(message = "{key_88d3009e9423426ebb84fb216300a8dd}")
    private String electrovalenceUnit;
}
