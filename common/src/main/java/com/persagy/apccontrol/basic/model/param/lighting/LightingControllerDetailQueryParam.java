package com.persagy.apccontrol.basic.model.param.lighting;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 照明-查询空间详情参数
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Data
@Schema(title = "照明-查询照明控制模块详情参数")
public class LightingControllerDetailQueryParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "模版类型")
    @NotNull(message = "{key_8356b8ff6af845ad99d6033c92e55c61}")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "空间ID")
    @NotBlank(message = "{key_c78c2ee89cbd491ea193306bff87f524}")
    private String spaceId;

    @Schema(description = "照明控制模块ID")
    @NotBlank(message = "{key_5b9d1425aef44897a34b161fd2cec6d2}")
    private String controllerId;

}
