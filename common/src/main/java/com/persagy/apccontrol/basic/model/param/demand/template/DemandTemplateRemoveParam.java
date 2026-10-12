package com.persagy.apccontrol.basic.model.param.demand.template;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * 需求管理-需求模版删除参数
 *
 * @author JiYinchuan
 * @since 7.250301.0
 */
@Data
@Schema(title = "需求管理-需求日历模版编辑参数")
public class DemandTemplateRemoveParam {

    @Schema(description = "模版类型")
    @NotNull(message = "{key_8356b8ff6af845ad99d6033c92e55c61}")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "待删除模版ID")
    @NotEmpty(message = "{key_cc4ad80c51ec4c3292047d8c87af4da7}")
    private Set<Long> toBeDelTemplateIds;

}
