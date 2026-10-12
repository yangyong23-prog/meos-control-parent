package com.persagy.apccontrol.basic.model.param.demand;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 查询需求屏蔽空间树参数
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
@Schema(title = "需求管理-查询需求屏蔽空间树参数")
public class DemandTreeShieldedObjectQueryParam {

    @Schema(description = "模版类型")
    @NotNull(message = "{key_8356b8ff6af845ad99d6033c92e55c61}")
    private DemandTemplateTypeEnum templateType;

}
