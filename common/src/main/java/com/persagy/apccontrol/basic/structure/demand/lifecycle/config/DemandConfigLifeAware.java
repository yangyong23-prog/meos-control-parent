package com.persagy.apccontrol.basic.structure.demand.lifecycle.config;

import com.persagy.apccontrol.basic.model.context.demand.DemandConfigAggregateInitByInstanceContext;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigByInstanceBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigSaveOrUpdateParam;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * 需求配置生命周期
 *
 * @param <T> 配置类型
 * @author JiYinchuan
 * @since 5.241102.0
 */
public interface DemandConfigLifeAware<T extends BaseDemandConfig> extends Ordered {

    // ------------------------- 聚合根生命周期 -------------------------

    /**
     * 聚合根初始化前置处理
     *
     * @param param 初始化参数
     * @since 5.241102.0
     */
    default void beforeContextInit(@NonNull DemandConfigByInstanceBatchQueryParam param) {
    }

    /**
     * 聚合根初始化后置处理
     *
     * @param context 初始化上下文
     * @since 5.241102.0
     */
    default void afterContextInit(
            @NonNull DemandConfigAggregateInitByInstanceContext<T> context) {
    }

    /**
     * 聚合根初始化前置处理
     *
     * @param context 初始化上下文
     * @since 5.241102.0
     */
    default void beforeAggregateInit(
            @NonNull DemandConfigAggregateInitByInstanceContext<T> context) {
    }

    /**
     * 聚合根初始化后置处理
     *
     * @param aggregates 聚合根
     * @since 5.241102.0
     */
    default void afterAggregateInit(
            @NonNull List<DemandConfigAggregate<T>> aggregates) {
    }

    /**
     * 聚合根继承配置前置处理
     *
     * @param aggregates 聚合根
     * @since 5.241102.0
     */
    default void beforeAggregateHandleInheritConfig(
            @NonNull List<DemandConfigAggregate<T>> aggregates) {
    }

    /**
     * 聚合根继承配置后置处理
     *
     * @param aggregates 聚合根
     * @since 5.241102.0
     */
    default void afterAggregateHandleInheritConfig(
            @NonNull List<DemandConfigAggregate<T>> aggregates) {
    }

    // ------------------------- 配置生命周期 -------------------------

    /**
     * 新增或编辑配置前置处理
     *
     * @param param 新增或编辑配置参数
     * @return 处理后的配置对象
     * @since 5.241102.0
     */
    default DemandConfigValueObject<T> beforeAddOrEdit(@NonNull DemandConfigSaveOrUpdateParam<T> param) {
        return param.getConfigData();
    }

    /**
     * 新增或编辑配置后置处理
     *
     * @param param 新增或编辑配置参数
     * @since 5.241102.0
     */
    default void afterAddOrEdit(@NonNull DemandConfigSaveOrUpdateParam<T> param) {
    }
}