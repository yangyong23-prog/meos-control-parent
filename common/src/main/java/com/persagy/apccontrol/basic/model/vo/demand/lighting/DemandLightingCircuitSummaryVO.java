package com.persagy.apccontrol.basic.model.vo.demand.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 需求管理-照明需求-照明回路统计
 *
 * @author zhangqiankun
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "需求管理-照明需求-照明回路统计VO")
public class DemandLightingCircuitSummaryVO {

    @Schema(description = "照明回路总数")
    private Integer total;

    @Schema(description = "照明回路开始数")
    private Integer openCount;

    @Schema(description = "照明回路关闭数")
    private Integer closeCount;

    @Schema(description = "照明回路反馈调节总数")
    private Integer feedbackAdjustmentCount;

    @Schema(description = "照明回路未配置数")
    private Integer unConfigCount;

    /**
     * 统计累加
     *
     * @param total
     * @param openCount
     * @param closeCount
     * @param unConfigCount
     * @author yuecaipu
     * @date 2024/3/25 14:01
     */
    public void plus(int total, int openCount, int closeCount, int unConfigCount) {
        this.total += total;
        this.openCount += openCount;
        this.closeCount += closeCount;
        this.unConfigCount += unConfigCount;
    }

    public void plus(DemandLightingCircuitSummaryVO summary) {
        this.total += summary.getTotal();
        this.openCount += summary.getOpenCount();
        this.closeCount += summary.getCloseCount();
        this.unConfigCount += summary.getUnConfigCount();
    }
}
