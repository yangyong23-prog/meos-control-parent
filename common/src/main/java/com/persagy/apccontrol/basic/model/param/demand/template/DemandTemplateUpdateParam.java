package com.persagy.apccontrol.basic.model.param.demand.template;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * 需求管理-需求日历模版编辑参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Schema(title = "需求管理-需求日历模版编辑参数")
public class DemandTemplateUpdateParam {

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "模版类型")
    @NotNull(message = "{key_8356b8ff6af845ad99d6033c92e55c61}")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "模版列表")
    @NotNull(message = "{key_5c810be35db0451dae244d85e3734a9c}")
    private List<Template> templates;

    @Schema(description = "待删除模版ID")
    private Set<Long> toBeDelTemplateIds;

    /**
     * 模版
     *
     * @author JiYinchuan
     * @since v6.0.0
     */
    @Data
    public static class Template {

        @Schema(description = "模版ID, 为空时则新增, 存在则编辑")
        private Long templateId;

        @Schema(description = "模版名称, 非预置模版时必填")
        private String templateName;

        @Schema(description = "是否启用")
        @NotNull(message = "{key_3c520583bf9643059a43db8c6a8b4456}")
        private Boolean isEnabled;

    }
}
