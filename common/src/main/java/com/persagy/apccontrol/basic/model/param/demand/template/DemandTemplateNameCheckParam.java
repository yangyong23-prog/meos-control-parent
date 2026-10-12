package com.persagy.apccontrol.basic.model.param.demand.template;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 需求管理-需求日历模版检查模版名称参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Accessors(chain = true)
@Schema(title = "需求管理-需求日历模版检查模版名称参数")
public class DemandTemplateNameCheckParam {

    /**
     * 模版名称允许长度
     */
    public static final int TEMPLATE_NAME_ALLOW_LENGTH = 50;

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "模版类型")
    @NotNull(message = "{key_8356b8ff6af845ad99d6033c92e55c61}")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "模版名称")
    @NotBlank(message = "{key_12e4d31bd767464b9c84aa614b6c1ff3}")
    private String templateName;

    @Schema(description = "模版ID, 存在时将过滤该ID记录")
    private Long templateId;

    @Schema(description = "是否抛出异常")
    private Boolean isThrows = false;

}
