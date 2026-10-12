package com.persagy.apccontrol.basic.model.param.demand;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 需求实例子集空间功能类型查询参数
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
@Schema(title = "需求实例子集空间功能类型查询参数")
public class DemandInstanceChildrenSpaceFuncTypeQueryParam {

    /**
     * 模版类型
     */
    @NotNull(message = "{key_8356b8ff6af845ad99d6033c92e55c61}")
    private DemandTemplateTypeEnum templateType;

    /**
     * 实例类型
     */
    @NotNull(message = "{key_3b13463321154c7695bc97753eeede5e}")
    private DemandInstanceTypeEnum instanceType;

    /**
     * 实例ID
     */
    @NotBlank(message = "{key_e8dc2fefc0e244ff95cc77a3352c1826}")
    private String instanceId;

}
