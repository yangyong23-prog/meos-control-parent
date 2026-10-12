package com.persagy.apccontrol.basic.model.vo.demand.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * 需求管理-照明需求-回回路配置视图
 *
 * @author zhangqiankun
 * @since 24Q1 T3S2-5.18.0
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "需求管理-照明需求-回回路配置视图")
public class DemandLightingCircuitView {

    @Schema(description = "回路状态统计")
    private DemandLightingCircuitSummaryVO summary;

    @Schema(description = "照明回路配置")
    private List<DemandLightingCircuitSettingVO> circuitSettings;

}
