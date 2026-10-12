package com.persagy.apccontrol.common.model.param.strategy.chiller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;


/**
 * @author yuyao
 */
@Data
@Schema(title = "查询时间区间有的供暖工冷季")
public class QuerySeasonOrder {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "开始时间")
    @NotNull(message = "{key_8ceaf15b76524876a9b10eea2e267297}")
    private LocalDate beginDateTime;

    @Schema(description = "结束时间")
    @NotNull(message = "{key_3313241b85ef48d9a3d2c43cd5e7930d}")
    private LocalDate endDateTime;

}
