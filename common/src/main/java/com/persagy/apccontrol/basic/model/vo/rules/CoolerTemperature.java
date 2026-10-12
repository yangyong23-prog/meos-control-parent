package com.persagy.apccontrol.basic.model.vo.rules;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.utils.ParameterCheck;
import com.persagy.framework.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @Description: 冷机出水温度
 * @author: yancheng
 * @date: 2023/3/31
 * @version: V1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoolerTemperature {

    @Schema(description = "出水温度")
    @NotNull(message = "{key_de05213538b94452b82c7b60761ac508}")
    @Valid
    private WaterTemperature waterTemperature;

    public void check() throws BusinessException {
        ParameterCheck.isNull(waterTemperature.getMax(), LocaleMessageUtil.getMessage("key_acb60e5c839344b9a9010778348957e4"));
        ParameterCheck.isNull(waterTemperature.getMin(), LocaleMessageUtil.getMessage("key_97892f167d8844c0b2a0226c890a1fb8"));
        ParameterCheck.isTrue(waterTemperature.getMin().compareTo(BigDecimal.valueOf(0)) < 0 || waterTemperature.getMin().compareTo(BigDecimal.valueOf(100)) > 0,
                LocaleMessageUtil.getMessage("key_0d37c7436c14423e8c723eabaceede84"));
        BigDecimal tempLower = waterTemperature.getMin().setScale(1, BigDecimal.ROUND_DOWN);
        ParameterCheck.isTrue(tempLower.compareTo(waterTemperature.getMin()) != 0, LocaleMessageUtil.getMessage("key_fabe50359bae433aa42df7dd116e74c0"));
        ParameterCheck.isTrue(waterTemperature.getMax().compareTo(BigDecimal.valueOf(0)) < 0 || waterTemperature.getMax().compareTo(BigDecimal.valueOf(100)) > 0,
                LocaleMessageUtil.getMessage("key_7e6daf8ba9004f5a83c5dfba66b8662f"));
        BigDecimal tempUpper = waterTemperature.getMax().setScale(1, BigDecimal.ROUND_DOWN);
        ParameterCheck.isTrue(tempUpper.compareTo(waterTemperature.getMax()) != 0, LocaleMessageUtil.getMessage("key_eb99f94a1741433d934e78cecf7a3c2d"));
        ParameterCheck.isTrue(waterTemperature.getMax().compareTo(waterTemperature.getMin()) < 0, LocaleMessageUtil.getMessage("key_104eff8064ec4efd8be0d82e002aa47e"));
    }
}
