package com.persagy.apccontrol.basic.model.vo.demand.template;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 需求管理-需求日历模版VO
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Schema(title = "需求管理-需求日历模版VO")
public class DemandTemplateVO {

    @Schema(description = "模版ID")
    private Long templateId;

    @Schema(description = "模版名称")
    private String templateName;

    @Schema(description = "需求类型")
    private DemandTypeEnum demandType;

    @Schema(description = "模版类型")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "模版标签")
    private String templateTag;

    @Schema(description = "是否为预置模版")
    private Boolean isInitialized;

    @Schema(description = "是否启用")
    private Boolean isEnabled;

}
