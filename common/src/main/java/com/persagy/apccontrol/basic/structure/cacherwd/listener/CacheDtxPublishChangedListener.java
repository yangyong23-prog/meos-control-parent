package com.persagy.apccontrol.basic.structure.cacherwd.listener;

import com.persagy.apccontrol.basic.model.base.CacheLockKey;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.message.BaseEvent;
import com.persagy.apccontrol.basic.model.message.dtx.DtxPublishMessageEvent;
import com.persagy.apccontrol.basic.model.message.dtx.DtxRulePublishMessageEvent;
import com.persagy.apccontrol.basic.structure.cacherwd.event.CacheRwdBuildTriggerEvent;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.evnet.DemandInstanceConfigChangeEvent;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 缓存DTX发布变更监听器
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheDtxPublishChangedListener {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 监听 DTX 发布事件
     *
     * @param event 事件
     * @since 7.260302.0
     */
    @Async
    @EventListener
    public void onDtxPublishEvent(@NonNull DtxPublishMessageEvent event) {
        this.publishCacheRwdBuildTriggerEvent(event, null);
    }

    /**
     * 监听 DTX 规则发布事件
     *
     * @param event 事件
     * @since 7.260302.0
     */
    @Async
    @EventListener
    public void onDtxRulePublishEvent(@NonNull DtxRulePublishMessageEvent event) {
        this.publishCacheRwdBuildTriggerEvent(event, null);
    }

    /**
     * 监听需求实例配置变更事件
     *
     * @param event 事件
     * @since 7.250302.0
     */
    @Async
    @EventListener
    public void onDemandInstanceConfigChangeEvent(@NonNull DemandInstanceConfigChangeEvent event) {
        DemandInstanceConfigChangeEvent.Param param = (DemandInstanceConfigChangeEvent.Param) event.getSource();
        // 只有空调类型才触发缓存刷新
        if (param.getTemplateType().toDemandTypeEnum() != DemandTypeEnum.AIRCONDITIONING) {
            return;
        }
        this.publishCacheRwdBuildTriggerEvent(event, RunningMonitorTypeEnum.AIR_TERMINAL);
    }

    /**
     * 发布缓存RWD构建触发事件
     *
     * @param event             基础事件
     * @param runningMonitorType 运行监测类型，为空时构建所有类型缓存
     * @since 7.260302.0
     */
    public void publishCacheRwdBuildTriggerEvent(
            @NonNull BaseEvent event,
            @Nullable RunningMonitorTypeEnum runningMonitorType) {
        CacheLockKey.Key key = CacheLockKey.DtxPublish.cache();
        event.executeWithLock(() -> "缓存DTX发布变更监听器", key, () -> this.applicationEventPublisher.publishEvent(
                new CacheRwdBuildTriggerEvent(runningMonitorType)));
    }
}
