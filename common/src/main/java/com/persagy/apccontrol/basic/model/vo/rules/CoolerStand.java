package com.persagy.apccontrol.basic.model.vo.rules;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.enumtype.CoolerRuleTypeEnum;
import com.persagy.apccontrol.basic.utils.ParameterCheck;
import com.persagy.framework.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * @Description: 冷机台数
 * @author: yancheng
 * @date: 2023/3/31
 * @version: V1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoolerStand {

    @Schema(description = "大冷机")
    @NotNull(message = "{key_a87d8f4111134afabc80f1c2da448dbe}")
    private LargeCooler largeCooler;

    @Schema(description = "小冷机")
    @NotNull(message = "{key_9a3f6d8f23634304b87e0267aaa9a4fc}")
    private SmallCooler smallCooler;

    public void check(Map<String, Integer> coolerMap) throws BusinessException {

        if (Objects.nonNull(largeCooler) && Objects.nonNull(largeCooler.getMax()) && Objects.nonNull(largeCooler.getMin())) {
            ParameterCheck.isTrue(largeCooler.getMin().compareTo(largeCooler.getMax()) > 0, LocaleMessageUtil.getMessage("key_22564435ad814225bbd90cc7c0a768fa"));
            largeCooler.setDefaultMax(coolerMap.get(CoolerRuleTypeEnum.LARGE_COOLER.getCode()) == null ? BigDecimal.ZERO : BigDecimal.valueOf(coolerMap.get(CoolerRuleTypeEnum.LARGE_COOLER.getCode())));

            if (largeCooler.getMin().compareTo(largeCooler.getDefaultMax()) > 0) {
                largeCooler.setMin(largeCooler.getDefaultMax());
            }
            if (largeCooler.getMax().compareTo(largeCooler.getDefaultMax()) > 0) {
                largeCooler.setMax(largeCooler.getDefaultMax());
            }
        }

        if (Objects.nonNull(smallCooler) && Objects.nonNull(smallCooler.getMax()) && Objects.nonNull(smallCooler.getMin())) {
            ParameterCheck.isTrue(smallCooler.getMin().compareTo(smallCooler.getMax()) > 0, LocaleMessageUtil.getMessage("key_22564435ad814225bbd90cc7c0a768fa"));
            smallCooler.setDefaultMax(coolerMap.get(CoolerRuleTypeEnum.SMALL_COOLER.getCode()) == null ? BigDecimal.ZERO : BigDecimal.valueOf(coolerMap.get(CoolerRuleTypeEnum.SMALL_COOLER.getCode())));

            if (smallCooler.getMin().compareTo(smallCooler.getDefaultMax()) > 0) {
                smallCooler.setMin(smallCooler.getDefaultMax());
            }
            if (smallCooler.getMax().compareTo(smallCooler.getDefaultMax()) > 0) {
                smallCooler.setMax(smallCooler.getDefaultMax());
            }
        }
    }
}
