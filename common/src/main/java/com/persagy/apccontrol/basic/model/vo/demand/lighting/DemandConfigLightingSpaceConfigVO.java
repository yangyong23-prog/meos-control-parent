package com.persagy.apccontrol.basic.model.vo.demand.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 需求配置-照明需求-空间配置数据
 *
 * @author zhangqiankun
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Schema(title = "需求配置-照明需求-空间配置数据")
public class DemandConfigLightingSpaceConfigVO {

    @Schema(description = "回路状态统计")
    private DemandLightingCircuitSummaryVO summary;

    @Schema(description = "空间类型配置")
    private List<DemandLightingSpaceFuncTypeSettingVO> settings;

}
