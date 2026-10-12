package com.persagy.apccontrol.basic.model.param.demand.scene;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 照明需求管理-场景保存参数
 *
 * @author zhangqiankun
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@ToString(callSuper = true)
@Schema(title = "照明需求管理-场景保存参数")
public class DemandSceneSaveParam {

    @Valid
    @Schema(description = "场景数据")
    @NotNull(message = "{key_7210c4ad1b3a4c48bc761e505ffadf8c}")
    private List<DemandSceneParam> configParams;

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "模板类型")
    private DemandTemplateTypeEnum demandTemplateType;

}
