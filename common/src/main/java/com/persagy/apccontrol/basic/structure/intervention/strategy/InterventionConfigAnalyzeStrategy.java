package com.persagy.apccontrol.basic.structure.intervention.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.vo.intervention.ConfigDetailsVo;
import com.persagy.apccontrol.basic.model.vo.intervention.InterventionQueryParam;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.intervention.enumeration.InterventionOperationTypeEnum;
import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;
import com.persagy.apccontrol.basic.structure.intervention.model.domain.InterventionConfigModel;
import org.springframework.lang.NonNull;

import java.util.function.Supplier;

/**
 * 干预配置解析策略
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
public interface InterventionConfigAnalyzeStrategy {

    /**
     * 运行监测类型
     *
     * @return 信息点类型
     * @since 5.240702.0
     */
    RunningMonitorTypeEnum runningMonitorType();

    /**
     * 继承配置类型
     *
     * @return 继承配置类型
     * @since 5.240702.0
     */
    InheritConfigTypeEnum inheritConfigType();

    /**
     * 配置类型
     *
     * @param <T> 配置类型
     * @return 配置类型
     * @since 5.240702.0
     */
    <T extends InterventionConfigData> Class<T> configType();

    /**
     * 创建配置对象
     *
     * @param <T> 配置类型
     * @return 配置对象
     * @since 5.240702.0
     */
    <T extends InterventionConfigData> Supplier<T> createSettingSupplier();

    /**
     * 解析配置对象
     *
     * @param configData 配置数据
     * @param <T>        配置对象类型
     * @return 配置对象
     * @since 5.240702.0
     */
    <T extends InterventionConfigData> T analyzeConfigObject(@NonNull JsonNode configData);

    /**
     * 发布干预变化事件
     *
     * @param operationType 操作类型
     * @param configModel   配置模型
     * @param <T>           配置对象类型
     * @since 5.240702.0
     */
    <T extends InterventionConfigData> void publishInterventionChangeEvent(
            @NonNull InterventionOperationTypeEnum operationType,
            @NonNull InterventionConfigModel<T> configModel);

    ConfigDetailsVo getConfigDetails(InterventionQueryParam param);
}
