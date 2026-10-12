package com.persagy.apccontrol.basic.structure.cacherwd.listener;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.structure.cacherwd.event.CacheRwdBuildTriggerEvent;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheRwdDomainService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 缓存构建事件监听器
 *
 * <p>监听 {@link CacheRwdBuildTriggerEvent} 事件并触发缓存构建；监听器在事件边界记录
 * 构建失败，避免缓存刷新异常影响 DTX 发布主流程。</p>
 *
 * @author wxf
 * @since 7.260320.0
 */
@Component
@RequiredArgsConstructor
public class CacheRwdBuildEventListener implements LogComponent {

    private final CacheRwdDomainService cacheRwdDomainService;

    /**
     * 处理缓存构建触发事件
     *
     * <p>缓存构建过程由 {@link CacheRwdDomainService#buildCache} 内部的分布式锁保护；
     * 构建异常在此记录，不继续抛回事件发布方。</p>
     *
     * @param event 缓存构建触发事件
     */
    @EventListener
    public void onCacheBuildTrigger(CacheRwdBuildTriggerEvent event) {
        this.logProjectInstance().info("收到缓存构建触发事件",
                Pair.of("runningMonitorType", event.getRunningMonitorType()));
        try {
            Boolean success = this.cacheRwdDomainService.buildCache(event.getRunningMonitorType());
            if (!Boolean.TRUE.equals(success)) {
                this.logProjectInstance().warn("缓存构建未全部成功",
                        Pair.of("runningMonitorType", event.getRunningMonitorType()));
            }
        } catch (RuntimeException exception) {
            // DTX 发布事件边界只记录缓存刷新失败，避免后台缓存故障同步影响发布请求。
            this.logProjectInstance().error("缓存构建执行失败", exception,
                    Pair.of("runningMonitorType", event.getRunningMonitorType()));
        }
    }

    @Override
    public String logTag() {
        return "缓存构建事件监听器";
    }
}
