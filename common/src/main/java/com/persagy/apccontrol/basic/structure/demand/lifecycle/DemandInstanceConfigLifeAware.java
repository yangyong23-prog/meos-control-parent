package com.persagy.apccontrol.basic.structure.demand.lifecycle;

import com.persagy.apccontrol.basic.structure.demand.model.instance.domain.DemandInstanceConfigEntity;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Set;

/**
 * 需求实例配置生命周期
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
public interface DemandInstanceConfigLifeAware extends Ordered {

    // ------------------------- 新增配置生命周期 -------------------------

    /**
     * 新增实例配置前置回调
     *
     * @param addEntities 新增实体
     * @since 5.241102.0
     */
    default void beforeAdd(@NonNull List<DemandInstanceConfigEntity> addEntities) {
    }

    /**
     * 新增实例配置后置回调
     *
     * @param addEntities 新增实体
     * @since 5.241102.0
     */
    default void afterAdd(@NonNull List<DemandInstanceConfigEntity> addEntities) {
    }

    // ------------------------- 编辑配置生命周期 -------------------------

    /**
     * 编辑实例配置前置回调
     *
     * @param editEntities 编辑实体
     * @since 5.241102.0
     */
    default void beforeEdit(@NonNull List<DemandInstanceConfigEntity> editEntities) {
    }

    /**
     * 编辑实例配置前置回调
     *
     * @param editEntities 编辑实体
     * @since 5.241102.0
     */
    default void afterEdit(@NonNull List<DemandInstanceConfigEntity> editEntities) {
    }

    // ------------------------- 删除配置生命周期 -------------------------

    /**
     * 删除实例配置前置回调
     *
     * @param delIds 删除ID
     * @since 5.241102.0
     */
    default void beforeDel(@NonNull Set<Long> delIds) {
    }

    /**
     * 删除实例配置前置回调
     *
     * @param delIds 删除ID
     * @since 5.241102.0
     */
    default void afterDel(@NonNull Set<Long> delIds) {
    }
}