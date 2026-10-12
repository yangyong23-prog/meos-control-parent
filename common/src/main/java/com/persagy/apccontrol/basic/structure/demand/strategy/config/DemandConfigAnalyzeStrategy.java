package com.persagy.apccontrol.basic.structure.demand.strategy.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigSaveOrUpdateParam;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import org.springframework.lang.NonNull;

import java.util.function.Supplier;

/**
 * 需求配置解析策略
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
public interface DemandConfigAnalyzeStrategy<T extends BaseDemandConfig> {

    /**
     * 模版类型
     *
     * @return 模版类型
     * @since 5.240902.0
     */
    DemandTemplateTypeEnum templateType();

    /**
     * 继承配置类型
     *
     * @return 继承配置类型
     * @since 5.240902.0
     */
    InheritConfigTypeEnum inheritConfigType();

    /**
     * 解析配置
     *
     * @param dataConfig 配置对象
     * @return 配置对象
     * @since 5.240902.0
     */
    DemandConfigValueObject<T> analyzeConfig(@NonNull JsonNode dataConfig);

    /**
     * 创建空配置
     *
     * @return 空配置
     * @since 5.241101.0
     */
    Supplier<T> emptyConfig();

    /**
     * 创建模版配置
     *
     * @param instanceType 实例类型
     * @return 模版配置对象
     * @since 5.241002.0
     */
    Supplier<T> defaultConfig(@NonNull DemandInstanceTypeEnum instanceType);

    /**
     * 保存或更新前处理
     *
     * @param param 保存或更新参数
     * @return 处理后的配置对象
     * @since 5.240902.0
     */
    default DemandConfigValueObject<T> beforeAddConfigHandler(@NonNull DemandConfigSaveOrUpdateParam<T> param) {
        return param.getConfigData();
    }
}