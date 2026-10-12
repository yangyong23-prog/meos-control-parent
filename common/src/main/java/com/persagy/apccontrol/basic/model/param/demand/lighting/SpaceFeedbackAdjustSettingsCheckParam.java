package com.persagy.apccontrol.basic.model.param.demand.lighting;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;


@Data
@Accessors(chain = true)
@Schema(title = "需求管理-空间是否允许反馈调节")
public class SpaceFeedbackAdjustSettingsCheckParam {

    @Schema(description = "需求模版类型")
    @NotNull(message = "{key_67d92d2b08624257a2b1303cf198a074}")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "空间ID")
    @NotNull(message = "{key_c78c2ee89cbd491ea193306bff87f524}")
    private String spaceId;


}
