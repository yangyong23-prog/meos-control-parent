package com.persagy.apccontrol.basic.model.param.demand.template;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 需求管理-需求日历模版查询参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Accessors(chain = true)
@Schema(title = "需求管理-需求日历模版查询参数")
public class DemandTemplateQueryParam implements BaseCheckParamProvider {

    @Schema(description = "模版类型")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "模版标签")
    private String templateTag;

    @Schema(description = "模版ID")
    private Long templateId;

    @Schema(description = "是否启用")
    private Boolean enabled;

}
