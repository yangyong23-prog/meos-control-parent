package com.persagy.apccontrol.basic.structure.demand.strategy.kitchen;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.strategy.AbstractDemandStrategyProcessor;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandSpacesQueryParam;
import org.jspecify.annotations.NonNull;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 需求管理-厨房排油烟需求策略实现
 *
 * @author JiYinchuan
 * @since v7.261001.0
 */
@Service(DemandKitchenRangeHoodStrategyImpl.BEAN_NAME)
@Validated
public class DemandKitchenRangeHoodStrategyImpl
        extends AbstractDemandStrategyProcessor
        implements DemandKitchenRangeHoodStrategy, PriorityOrdered {

    /**
     * 策略Bean名称
     *
     * @since v7.261001.0
     */
    public static final String BEAN_NAME = "demandKitchenRangeHoodStrategyImpl";

    @Override
    public List<DtpRwdSpaceDTO> listDemandSpace(@NonNull DemandSpacesQueryParam param) {
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        RunningMonitorSecondTypeEnum runningMonitorSecondType = templateType.toRunningMonitorSecondTypeEnum();
        RunningMonitorSpaceQueryParam spaceQueryParam = param.toRunningMonitorSpaceQueryParam();
        spaceQueryParam.setRunningMonitorSecondType(runningMonitorSecondType);
        return runningMonitorSecondType.toRunningMonitorTypeEnum().getRunningMonitorStrategy().listSpaces(spaceQueryParam);
    }

    @Override
    public DemandTypeEnum demandType() {
        return DemandTypeEnum.KITCHEN_RANGE_HOOD;
    }

    @Override
    public String logTag() {
        return "需求管理-厨房排油烟需求策略";
    }
}
