package com.persagy.apccontrol.basic.model.vo.runanalysis;

import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectConfigDto;
import lombok.Data;

import java.util.List;

@Data
public class ValueSelectionVO {

    /**
     * 价值选择名称
     */
    private String name;

    /**
     * 价值选择类型
     * VALUE_SELECT_LOWEST_CAST 费用最低
     * VALUE_SELECT_LOWEST_CARBON_EMISSIONS 碳排放最低
     * VALUE_SELECT_LOWEST_ENERGY 能耗最低
     * VALUE_SELECT_QUALITY_FIRST 品质优先
     */
    private BasicsConfigTypeEnum valueSelectionType;

    /**
     * 满足率配置
     */
    private List<ValueSelectConfigDto> tempFillRateConfig;
}
