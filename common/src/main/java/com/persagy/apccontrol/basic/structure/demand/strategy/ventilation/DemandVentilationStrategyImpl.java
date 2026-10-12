package com.persagy.apccontrol.basic.structure.demand.strategy.ventilation;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.strategy.AbstractDemandStrategyProcessor;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandSpacesQueryParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.PriorityOrdered;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 需求管理-通风需求策略实现
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Slf4j
@Service(DemandVentilationStrategyImpl.BEAN_NAME)
@Validated
public class DemandVentilationStrategyImpl
        extends AbstractDemandStrategyProcessor
        implements DemandVentilationStrategy, PriorityOrdered {

    public static final String BEAN_NAME = "demandVentilationStrategyImpl";

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
        return DemandTypeEnum.VENTILATION;
    }

    @Override
    public String logTag() {
        return "需求管理-通风需求策略";
    }
}
