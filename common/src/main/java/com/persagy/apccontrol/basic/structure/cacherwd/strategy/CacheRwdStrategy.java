package com.persagy.apccontrol.basic.structure.cacherwd.strategy;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;

/**
 * 物理世界对象缓存策略接口
 *
 * <p>------------------------- 公共方法 -------------------------
 * <ul>
 *     <li><b>runningMonitorType</b> - 运行监测类型</li>
 *     <li><b>buildCacheData</b> - 构建缓存数据</li>
 * </ul>
 *
 * @author wxf
 * @since 7.260302.0
 */
public interface CacheRwdStrategy extends LogComponent {

    /**
     * 是否仅在全局公共缓存阶段执行。
     *
     * <p>公共项目结构等策略不绑定任何一级运行监测类型，不能随着每个业务类型重复执行。</p>
     *
     * @return 仅公共阶段执行时返回 {@code true}
     * @since v7.260802.0
     */
    default boolean onlyBuildInCommonPhase() {
        return false;
    }

    /**
     * 运行监测类型
     *
     * @return 运行监测类型
     */
    RunningMonitorTypeEnum runningMonitorType();

    /**
     * 构建缓存数据
     *
     * @param runningMonitorType 运行监测类型
     * @return 是否构建成功
     */
    boolean buildCacheData(RunningMonitorTypeEnum runningMonitorType);
}
