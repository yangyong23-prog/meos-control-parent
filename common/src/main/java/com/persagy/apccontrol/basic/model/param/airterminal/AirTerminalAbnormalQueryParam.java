package com.persagy.apccontrol.basic.model.param.airterminal;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
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
@Schema(title = "实例查询参数")
public class AirTerminalAbnormalQueryParam implements BaseCheckParamProvider {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "实例ID")
    @NotBlank(message = "{key_e8dc2fefc0e244ff95cc77a3352c1826}")
    private String instanceId;

    @Schema(description = "实例类型")
    @NotNull(message = "{key_3b13463321154c7695bc97753eeede5e}")
    private InstanceTypeEnum instanceType;

    @Override
    public void checkAndSet() {
        if (instanceType != InstanceTypeEnum.SPACE) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_50e34370fe0f4b59b37182d7062239a2"));
        }
    }
}
