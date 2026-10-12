package com.persagy.apccontrol.basic.model.param.runanalysis;

import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class EnergyCoverDataParam extends RunAnalysisBaseParam {

    /**
     * 价值选择类型
     * VALUE_SELECT_LOWEST_CAST 费用最低
     * VALUE_SELECT_LOWEST_CARBON_EMISSIONS 碳排放最低
     * VALUE_SELECT_LOWEST_ENERGY 能耗最低
     * VALUE_SELECT_QUALITY_FIRST 品质优先
     */
    @NotNull(message = "{key_ae0642190f6342c19e5e7793e3b4f76f}")
    private BasicsConfigTypeEnum valueSelectionType;

}
