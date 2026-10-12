package com.persagy.apccontrol.basic.model.dto.demand.lighting;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingTypeSettingModeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.circuit.DtpRwdCircuitLampFuncTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 需求管理-照明需求-照明灯具类型配置DTO
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 * @since v5.240402.0 废弃该层级
 */
@Deprecated
@Data
@Schema(title = "需求管理-照明需求-照明灯具类型配置DTO")
public class DemandLightingLampFuncTypeSettingDTO implements BaseCheckParamProvider {

    @Schema(description = "照明回路灯具功能类型")
    //@NotNull(message = "照明回路灯具功能类型不能为空")
    private DtpRwdCircuitLampFuncTypeEnum lampFuncType;

    @Schema(description = "类型配置模式")
    //@NotNull(message = "类型配置模式不能为空")
    private DemandLightingTypeSettingModeEnum settingMode;

    @Schema(description = "照明回路配置")
    @Valid
    private List<DemandLightingCircuitSettingDTO> circuitSettings;

    @Override
    public void checkAndSet() {
        if (CollectionUtils.isNotEmpty(circuitSettings)) {
            if (checkRepeat(circuitSettings.stream()
                    .map(DemandLightingCircuitSettingDTO::getCircuitId)
                    .collect(Collectors.toList()))) {
                throw new ValidationException(LocaleMessageUtil.getMessage("key_1508333f1b964a3b8f00c95247cd7e0c"));
            }
        }
    }
}
