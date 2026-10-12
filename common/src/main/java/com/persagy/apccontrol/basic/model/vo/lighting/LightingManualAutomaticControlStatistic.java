package com.persagy.apccontrol.basic.model.vo.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 辅助统计类
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2025/4/16 10:38
 * @since 7.250202.0
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LightingManualAutomaticControlStatistic {

    @Schema(description = "自动数量")
    private Integer autoCount = 0;
    @Schema(description = "自动设备id集合")
    private List<String> autoIds = new ArrayList<>();
    @Schema(description = "手动数量")
    private Integer manualCount = 0;
    @Schema(description = "手动设备id集合")
    private List<String> manualIds = new ArrayList<>();

    public LightingManualAutomaticControlStatistic(Integer autoCount, List<String> autoIds, Integer manualCount, List<String> manualIds) {
        this.autoCount = autoCount;
        this.autoIds = new ArrayList<>(autoIds);
        this.manualCount = manualCount;
        this.manualIds = new ArrayList<>(manualIds);
    }

    public void accumulate(LightingManualAutomaticControlStatistic other) {
        this.autoCount += other.autoCount;
        this.manualCount += other.manualCount;
        this.autoIds.addAll(other.autoIds);
        this.manualIds.addAll(other.manualIds);
    }
}
