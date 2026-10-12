package com.persagy.apccontrol.basic.model.param.demand.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * 照明需求管理-模板查询条件
 *
 * @author zhangqiankun
 * @since 24Q1 T3S1-5.17.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(title = "照明需求管理-场景查询条件")
public class DemandLightingTemplateSceneParam extends DemandLightingTemplateIdParam {

    @Schema(description = "场景ID")
    @NotNull(message = "{key_7fe82377eb104dfa8424cffc012e5006}")
    private Long sceneId;

}
