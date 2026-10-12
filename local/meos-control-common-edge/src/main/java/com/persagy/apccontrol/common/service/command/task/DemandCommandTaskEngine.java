package com.persagy.apccontrol.common.service.command.task;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.service.engine.BasicsTaskEngine;
import com.persagy.apccontrol.basic.structure.intervention.event.lighting.InterventionLightingConfigChangeEvent;
import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.common.model.context.DemandCommandTaskCtx;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 照明需求计算任务
 */
public interface DemandCommandTaskEngine extends BasicsTaskEngine {


    /**
     * 通过场景 ID 触发计算
     *
     * @param ctx 任务上下文
     */
    void execute(DemandCommandTaskCtx ctx);
    
    
    /**
     * 需求干预计算
     */
    void executeIntervene(RunningMonitorSecondTypeEnum runningMonitorSecondType);

    @TransactionalEventListener(value = InterventionLightingConfigChangeEvent.class)
    default <T extends InterventionConfigData> void eventListener(InterventionLightingConfigChangeEvent<T> event) {
        BusinessThreadPoolExecutor.ttlExecutor().execute(() -> {
            executeIntervene(event.getConfigModel().getRunningMonitorSecondType());
        });
    }
}
