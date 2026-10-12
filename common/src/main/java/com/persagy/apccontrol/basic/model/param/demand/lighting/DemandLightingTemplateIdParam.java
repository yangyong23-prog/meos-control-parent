package com.persagy.apccontrol.basic.model.param.demand.lighting;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 照明需求管理-模板查询条件
 *
 * @author zhangqiankun
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Schema(title = "照明需求管理-模板查询条件")
public class DemandLightingTemplateIdParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "模板ID")
    //@NotNull(message = "模板ID，不能为空")
    private Long templateId;

    @Schema(description = "模板类型")
    private DemandTemplateTypeEnum demandTemplateType;

}
