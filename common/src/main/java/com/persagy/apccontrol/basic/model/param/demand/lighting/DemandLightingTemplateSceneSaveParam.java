package com.persagy.apccontrol.basic.model.param.demand.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.Valid;
import java.util.List;

/**
 * 照明需求管理-场景保存参数
 *
 * @author zhangqiankun
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "照明需求管理-场景保存参数")
public class DemandLightingTemplateSceneSaveParam extends DemandLightingTemplateIdParam {

    @Valid
    @Schema(description = "场景数据")
    private List<DemandLightingTemplateSceneData> configParams;

}
