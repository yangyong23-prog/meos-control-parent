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
 * @Description: 冷机开关机约束
 * @author: yancheng
 * @date: 2023/3/31
 * @version: V1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoolerSwitch {

    @Schema(description = "开机")
    @NotNull(message = "{key_66c6388935524dadb8cba4a4384cdff6}")
    @Valid
    private PowerUp powerUp;

    @Schema(description = "关机")
    @NotNull(message = "{key_2ac717971c224938bbb2b55bb3f61f2e}")
    @Valid
    private ShutDown shutDown;

    public void check() throws BusinessException {
        ParameterCheck.isNull(powerUp.getMax(), LocaleMessageUtil.getMessage("key_382b8b9957054fb8b75b27ea132524a7"));
        ParameterCheck.isNull(powerUp.getMin(), LocaleMessageUtil.getMessage("key_2234e28d115e4e9798c3015129bac6dd"));
        ParameterCheck.isNull(shutDown.getMax(), LocaleMessageUtil.getMessage("key_43cfe083767f4f058d84f1b68c6b06c4"));
        ParameterCheck.isNull(shutDown.getMin(), LocaleMessageUtil.getMessage("key_facf05856ec94ec2a94838e87957c710"));

        if (powerUp.getMin().compareTo(BigDecimal.valueOf(0)) < 0 || powerUp.getMin().compareTo(BigDecimal.valueOf(240)) > 0) {
            powerUp.setMin(BigDecimal.valueOf(240));
        }
        BigDecimal powerUpTempLower = powerUp.getMin().setScale(0, BigDecimal.ROUND_DOWN);
        ParameterCheck.isTrue(powerUpTempLower.compareTo(powerUp.getMin()) != 0, LocaleMessageUtil.getMessage("key_0e9563c8f0214ccda7e2ea75562d2191"));
        if (powerUp.getMax().compareTo(BigDecimal.valueOf(0)) < 0 || powerUp.getMax().compareTo(BigDecimal.valueOf(240)) > 0) {
            powerUp.setMax(BigDecimal.valueOf(240));
        }
        BigDecimal powerUpTempUpper = powerUp.getMax().setScale(0, BigDecimal.ROUND_DOWN);
        ParameterCheck.isTrue(powerUpTempUpper.compareTo(powerUp.getMax()) != 0, LocaleMessageUtil.getMessage("key_f1b39541ea434a1b81be8e1a88b471f9"));
        ParameterCheck.isTrue(powerUp.getMax().compareTo(powerUp.getMin()) < 0, LocaleMessageUtil.getMessage("key_2d4cb3c63cf64ee58cac3522494a4464"));


        if (shutDown.getMin().compareTo(BigDecimal.valueOf(0)) < 0 || shutDown.getMin().compareTo(BigDecimal.valueOf(240)) > 0) {
            shutDown.setMin(BigDecimal.valueOf(240));
        }
        BigDecimal shutDownTempLower = shutDown.getMin().setScale(0, BigDecimal.ROUND_DOWN);
        ParameterCheck.isTrue(shutDownTempLower.compareTo(shutDown.getMin()) != 0, LocaleMessageUtil.getMessage("key_d92cbb8e9c76415bb87b960a4c68524e"));
        if (shutDown.getMax().compareTo(BigDecimal.valueOf(0)) < 0 || shutDown.getMax().compareTo(BigDecimal.valueOf(240)) > 0) {
            shutDown.setMax(BigDecimal.valueOf(240));
        }
        BigDecimal shutDownTempUpper = shutDown.getMax().setScale(0, BigDecimal.ROUND_DOWN);
        ParameterCheck.isTrue(shutDownTempUpper.compareTo(shutDown.getMax()) != 0, LocaleMessageUtil.getMessage("key_5090068045d941b8a576d389b3f7b8b7"));
        ParameterCheck.isTrue(shutDown.getMax().compareTo(shutDown.getMin()) < 0, LocaleMessageUtil.getMessage("key_547a7577d85c4d1ea6df558e0f547a34"));
    }
}
