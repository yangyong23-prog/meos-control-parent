package com.persagy.apccontrol.basic.structure.cacherwd.strategy;

/**
 * 物理世界对象缓存策略抽象基类
 *
 * @author wxf
 * @since 7.260302.0
 */
public abstract class BaseCacheRwdStrategy implements CacheRwdStrategy {

    @Override
    public String logTag() {
        return "物理世界缓存-" + runningMonitorType().getReasonPhrase() + "策略";
    }
}
