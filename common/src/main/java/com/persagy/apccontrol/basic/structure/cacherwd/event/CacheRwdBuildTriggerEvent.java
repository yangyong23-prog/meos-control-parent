package com.persagy.apccontrol.basic.structure.cacherwd.event;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEvent;

/**
 * 缓存构建触发事件
 *
 * @author wxf
 * @since 7.260320.0
 */
@Getter
public class CacheRwdBuildTriggerEvent extends ApplicationEvent {

    /**
     * 运行监测类型，为空时构建所有类型缓存
     */
    private final @Nullable RunningMonitorTypeEnum runningMonitorType;

    /**
     * 构造器
     *
     * @param runningMonitorType 运行监测类型，为空时构建所有类型缓存
     */
    public CacheRwdBuildTriggerEvent(@Nullable RunningMonitorTypeEnum runningMonitorType) {
        super("");
        this.runningMonitorType = runningMonitorType;
    }
}
