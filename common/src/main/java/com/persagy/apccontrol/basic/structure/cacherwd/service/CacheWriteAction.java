package com.persagy.apccontrol.basic.structure.cacherwd.service;

/**
 * 已在事务外准备完成、可在缓存替换阶段执行的本地写入动作。
 *
 * <p>实现不得在 {@link #execute()} 中发起远程计算；该接口只承载对象、明细、规则和关系等
 * 本地 SQL，使新版缓存可以纳入单 Key 短事务，历史缓存也可以保持原有顺序执行边界。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@FunctionalInterface
public interface CacheWriteAction {

    /**
     * 执行已经准备好的本地缓存写入。
     *
     * @since v7.260701.0
     */
    void execute();
}
