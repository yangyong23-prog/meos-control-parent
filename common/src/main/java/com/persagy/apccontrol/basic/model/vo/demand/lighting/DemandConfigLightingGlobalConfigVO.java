package com.persagy.apccontrol.basic.model.vo.demand.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 需求配置-照明需求-全局配置数据
 *
 * @author zhangqiankun
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Schema(title = "需求配置-照明需求-全局配置数据")
public class DemandConfigLightingGlobalConfigVO {

    @Schema(description = "当前配置ID值")
    private String configId = "global";

    @Schema(description = "回路状态统计")
    private DemandLightingCircuitSummaryVO summary;

    @Schema(description = "全局配置")
    private List<DemandLightingFuncTypeSettingVO> funcTypeSettings;

}
