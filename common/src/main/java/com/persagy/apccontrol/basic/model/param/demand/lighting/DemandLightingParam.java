package com.persagy.apccontrol.basic.model.param.demand.lighting;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 照明需求管理-实例查询条件
 *
 * @author yuecaipu
 * @since v5_240702
 */
@Data
@Schema(title = "照明-查询条件")
public class DemandLightingParam {

    @Schema(description = "项目ID")
    @NotNull(message = "{key_88c2e95dd2164d089bf198baa2106998}")
    private String projectId;

    @Schema(description = "实例ID")
    @NotBlank(message = "{key_ea24192e2d4649279b763b2b19f83742}")
    private String instanceId;

    @Schema(description = "实例类型")
    @NotNull(message = "{key_1e47d670c73d4ab2803db5dc2965e783}")
    private DemandInstanceTypeEnum instanceType;

    @Schema(description = "需求模版类型）")
    @NotNull(message = "{key_aa218b5f07c4414a89d5514d10bd4e67}")
    private DemandTemplateTypeEnum demandTemplateType;

    @Schema(description = "干预配置ID")
    private Long interventionId;

}
