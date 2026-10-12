package com.persagy.apccontrol.common.structure.intervention.infrastructure.impl;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.structure.intervention.enumeration.InterventionOperationTypeEnum;
import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;
import com.persagy.apccontrol.basic.structure.intervention.model.domain.InterventionConfigModel;
import com.persagy.apccontrol.common.resource.InterventionConfigOperationResourceService;
import com.persagy.apccontrol.common.structure.intervention.infrastructure.InterventionConfigOperationInfrastructure;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 干预需求配置基础设施服务
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InterventionConfigOperationInfrastructureImpl<T extends InterventionConfigData>
        implements InterventionConfigOperationInfrastructure<T>, LogComponent {

    private final InterventionConfigOperationResourceService interventionConfigOperationResourceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean release(@NonNull InterventionConfigModel<T> model) {
        Long interventionConfigId = model.getIdentifier().getInterventionConfigId();
        RunningMonitorSecondTypeEnum runningMonitorSecondType = model.getRunningMonitorSecondType();
        // 当前场景正在生效中时, 将结束时间设为开始时间, 否则直接删除
        boolean result;
        if (model.isEffectiveByNow()) {
            model.resetEndTimeToNow();
            result = interventionConfigOperationResourceService.editEndTime(interventionConfigId,
                    model.getDateTimeRange().getEndDateTime());
        } else {
            result = interventionConfigOperationResourceService.del(interventionConfigId);
        }
        if (result) {
            runningMonitorSecondType.toRunningMonitorTypeEnum().getInterventionConfigAnalyzeStrategy()
                    .publishInterventionChangeEvent(InterventionOperationTypeEnum.RELEASE, model);
        }
        return result;
    }

    @Override
    public String logTag() {
        return "干预配置模型-基础设施";
    }
}
