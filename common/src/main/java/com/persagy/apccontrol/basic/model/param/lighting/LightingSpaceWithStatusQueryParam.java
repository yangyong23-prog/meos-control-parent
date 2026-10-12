package com.persagy.apccontrol.basic.model.param.lighting;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class LightingSpaceWithStatusQueryParam {

    @NotBlank(message = "{key_79ae698d8ca34f22a0784247c6543796}")
    @Schema(description = "项目ID")
    private String projectId;

    @ApiModelProperty("实例id,为空时查询所有")
    private String instanceId;

    @ApiModelProperty("实例类型,为空时查询所有")
    private DemandInstanceTypeEnum instanceType;

    @NotNull(message = "{key_6abfa7ec1104485d9a7d9748ac7dd369}")
    @ApiModelProperty("模板类型")
    private DemandTemplateTypeEnum templateType;

}
