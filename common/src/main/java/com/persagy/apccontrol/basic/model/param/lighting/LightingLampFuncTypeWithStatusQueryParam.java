package com.persagy.apccontrol.basic.model.param.lighting;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 照明灯具功能类型带状态查询参数
 *
 * @author JiYinchuan
 * @since 5.240501.0
 */
@Data
@Schema(title = "照明灯具功能类型带状态查询参数")
public class LightingLampFuncTypeWithStatusQueryParam {

    @NotBlank(message = "{key_79ae698d8ca34f22a0784247c6543796}")
    @Schema(description = "项目ID")
    private String projectId;

    @NotNull(message = "{key_6abfa7ec1104485d9a7d9748ac7dd369}")
    @ApiModelProperty("模板类型")
    private DemandTemplateTypeEnum templateType;

}
