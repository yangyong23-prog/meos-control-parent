package com.persagy.apccontrol.basic.model.vo.demand.lighting;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 需求管理-照明需求-当前场景模式
 *
 * @author zhangqiankun
 * @since 24Q1 T3S2-5.18.0
 */
@Getter
@Setter
@ToString
@Schema(title = "需求管理-照明需求-当前照明类型")
public class DemandLightingTemplateTypeVO {

    @Schema(description = "照明类型")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "照明类型名称")
    private String templateTypeName;

    @Schema(description = "场景配置模式")
    private List<DemandLightingSceneModel> sceneModes;

    @Schema(description = "回路数量统计")
    private DemandLightingCircuitSummaryVO summary;

    @Schema(description = "照明回路功能类型分组树")
    private List<DemandLightingFuncTypeSettingVO> funcTypeSettings;

    public String getTemplateTypeName() {
        return templateType == null ? null : templateType.getReasonPhrase();
    }

}
