package com.persagy.apccontrol.basic.model.vo.strategy.chiller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 运行冷机
 *
 * @since v5.7.0
 */
@Data
@Schema(title = "运行冷机")
public class RunningCoolerVO {

    @Schema(description = "运行的大机台数")
    private Integer runningChillerLNum;

    @Schema(description = "运行的小机台数")
    private Integer runningChillerSNum;

    @Schema(description = "最大额定制冷量")
    private BigDecimal ratedCoolMax;

    @Schema(description = "最小额定制冷量")
    private BigDecimal ratedCoolMin;

    public Integer getRunningChillerLNum() {
        if (this.runningChillerLNum == null) {
            return 0;
        }
        return runningChillerLNum;
    }

    public Integer getRunningChillerSNum() {
        if (this.runningChillerSNum == null) {
            return 0;
        }
        return runningChillerSNum;
    }
}
