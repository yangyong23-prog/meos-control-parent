package com.persagy.apccontrol.basic.model.dto.demand.airconditioning;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ValueSelectionTrendTableDto {

    @Schema(name = "项目id")
    private String projectId;

    @Schema(name = "营业时间")
    private LocalTime openTime;

    @Schema(name = "闭店时间")
    private LocalTime closeTime;

    @Schema(name = "数据列表")
    private List<ElecData> elecDataList = new ArrayList<>();

    @Schema(name = "下四分位数")
    private BigDecimal lowerQuartile;

    @Schema(name = "中位数")
    private BigDecimal median;

    @Schema(name = "上四分位数")
    private BigDecimal upperQuartile;

    @Schema(name = "平均值")
    private BigDecimal averageValue;

    @Data
    @NoArgsConstructor
    public static class ElecData{
        @Schema(name = "数据时间")
        private LocalDateTime dataTime;

        @Schema(name = "数据值")
        private BigDecimal dataValue;

    }
}
