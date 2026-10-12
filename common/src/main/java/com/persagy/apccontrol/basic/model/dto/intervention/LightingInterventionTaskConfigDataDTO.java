package com.persagy.apccontrol.basic.model.dto.intervention;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import com.persagy.apccontrol.basic.utils.ParameterCheck;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

/**
 * 照明干预的配置对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class LightingInterventionTaskConfigDataDTO extends InterventionTaskConfigDataDTO {
    
    @Schema(description = "照明回路ID")
    private String circuitId;
    
    @Schema(description = "照明回路配置模式")
    private DemandLightingCircuitSettingModeEnum settingMode;
    
    @Override
    public void check() {
        ParameterCheck.isTrue(StringUtils.isEmpty(circuitId), LocaleMessageUtil.getMessage("key_d76ea9b42948435aabcf79f837dc128f"));
        ParameterCheck.isTrue(settingMode == null, LocaleMessageUtil.getMessage("key_e59ff1c22edb44158ece1b081c425861"));
    }
}
