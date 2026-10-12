package com.persagy.apccontrol.basic.model.param.lighting;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.ValidationException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 查询设备状态
 *
 * @author yang
 * @version 1.0.0
 * @date 2024/4/29 14:23
 * @since 5.240402.0
 */
@Data
@Schema(title = "照明-实例查询参数")
public class LightingInstanceQueryParam implements BaseCheckParamProvider {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "实例ID")
    private String instanceId;

    @Schema(description = "实例类型")
    private DemandInstanceTypeEnum instanceType;

    @Schema(description = "需求模板类型")
    @NotNull(message = "{key_67d92d2b08624257a2b1303cf198a074}")
    private DemandTemplateTypeEnum demandTemplateType;

    @Override
    public void checkAndSet() {
        if (instanceType != DemandInstanceTypeEnum.SPACE) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_50e34370fe0f4b59b37182d7062239a2"));
        }
    }
}
