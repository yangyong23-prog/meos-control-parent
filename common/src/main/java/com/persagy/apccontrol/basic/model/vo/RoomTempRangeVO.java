package com.persagy.apccontrol.basic.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ：wzq
 * @description：
 * @date ：Created in 2023/10/10 3:37 PM
 */
@Data
public class RoomTempRangeVO {

    @Schema(name = "最小值")
    private BigDecimal valueMin;

    @Schema(name = "范围中是否包含最小值")
    private Boolean isIncludeMin;

    @Schema(name = "最大值")
    private BigDecimal valueMax;

    @Schema(name = "范围中是否包含最大值")
    private Boolean isIncludeMax;

    @Schema(name = "范围")
    private String range;
}
