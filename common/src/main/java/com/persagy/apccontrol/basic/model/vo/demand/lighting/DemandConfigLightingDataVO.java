package com.persagy.apccontrol.basic.model.vo.demand.lighting;

import com.persagy.apccontrol.basic.model.vo.lighting.LightingSpaceFeedbackAdjustSettingVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 需求配置-照明需求-配置数据
 *
 * @author zhangqiankun
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Schema(title = "需求配置-照明需求-配置数据")
public class DemandConfigLightingDataVO {

    @Schema(description = "引用来源ID")
    private String sourceId;

    @Schema(description = "引用来源名称")
    private String sourceName;

    @Schema(description = "全局配置")
    private DemandConfigLightingGlobalConfigVO global;

    @Schema(description = "空间类型配置")
    private DemandConfigLightingSpaceConfigVO space;

    @Schema(description = "空间反馈条件配置")
    private LightingSpaceFeedbackAdjustSettingVO spaceFeedbackAdjust;

}
