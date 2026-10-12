package com.persagy.apccontrol.basic.structure.demand.model.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.base.domain.AggregateRoot;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInheritModeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.DemandSpaceFuncTypeConfig;
import com.persagy.apccontrol.basic.structure.demand.model.domain.identifier.DemandConfigIdentifier;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandConfigGroupEntity;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandSceneEntity;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.DemandConfigAnalyzeStrategy;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.beans.PropertyDescriptor;
import java.time.LocalDate;
import java.util.*;

/**
 * 需求配置聚合根
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
public class DemandConfigAggregate<T extends BaseDemandConfig> implements AggregateRoot<DemandConfigIdentifier> {

    /**
     * 标识符
     */
    private DemandConfigIdentifier identifier;

    /**
     * 自身配置
     */
    @Nullable
    private DemandConfigValueObject<T> config;

    /**
     * 上级配置
     */
    @Nullable
    private DemandConfigValueObject<T> parentConfig;

    /**
     * 最终配置
     */
    @Nullable
    private DemandConfigValueObject<T> finalConfig;

    /**
     * 是否为自定义配置
     */
    @Setter(AccessLevel.NONE)
    private boolean customizedConfig;

    /**
     * 继承实例, 继承对象 -> 继承类型
     *
     * <ul>
     *     <li><b>对象继承</b> - 最多存在一个</li>
     *     <li><b>字段继承</b> - 最多存在三个, 例如为空间时最多存在楼层/建筑/项目</li>
     * </ul>
     */
    private LinkedHashMap<DemandInstanceValueObject, Set<DemandInheritModeEnum>> inheritInstanceAndModes = new LinkedHashMap<>();

    /**
     * 是否为自定义配置
     *
     * @return 是否为自定义配置
     * @since 5.240902.0
     */
    @SuppressWarnings("unused")
    public boolean isCustomizedConfig() {
        InheritConfigTypeEnum inheritConfigType = getIdentifier().getInheritConfigType();
        switch (inheritConfigType) {
            case OBJECT:
                return Objects.nonNull(config);
            case FIELD:
                if (Objects.isNull(config)) {
                    return false;
                }
                T globalConfig = config.getGlobalConfig();
                if (Objects.nonNull(globalConfig) && globalConfig.anyFieldNotEmpty()) {
                    return true;
                }
                List<DemandSpaceFuncTypeConfig<T>> spaceFuncTypeConfigs = config.getSpaceFuncTypeConfigs();
                return CollectionUtils.isNotEmpty(spaceFuncTypeConfigs) && spaceFuncTypeConfigs.stream()
                        .filter(item -> Objects.nonNull(item.getConfig()))
                        .anyMatch(item -> item.getConfig().anyFieldNotEmpty());
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(
                        inheritConfigType.getClass().getSimpleName(), inheritConfigType);
        }
    }

    /**
     * 设置自身配置
     *
     * @param config 自身配置
     * @since 5.241002.0
     */
    public void setConfig(@Nullable DemandConfigValueObject<T> config) {
        this.config = config;
        handleFinalConfig();
    }

    /**
     * 设置自身配置
     *
     * @param parentConfig 父级配置
     * @since 5.241002.0
     */
    public void setParentConfig(@Nullable DemandConfigValueObject<T> parentConfig) {
        this.parentConfig = parentConfig;
        handleFinalConfig();
    }

    /**
     * 创建聚合根
     *
     * @param templateType 模版类型
     * @param templateTag  模版标签
     * @param templateId   模板ID
     * @param date         配置日期
     * @param scene        场景实体
     * @param instance     实例
     * @param configData   配置内容
     * @param <T>          配置类型
     * @return 聚合根
     * @since 5.240902.0
     */
    public static <T extends BaseDemandConfig> DemandConfigAggregate<T> of(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull String templateTag,
            @NonNull Long templateId,
            @NonNull LocalDate date,
            @Nullable DemandSceneEntity scene,
            @NonNull DemandInstanceValueObject instance,
            @Nullable JsonNode configData) {
        return of(templateType, templateTag, templateId, date,
                scene, null, null, instance, configData);
    }

    /**
     * 创建聚合根
     *
     * @param templateType                         模版类型
     * @param templateTag                          模版标签
     * @param templateId                           模板ID
     * @param date                                 配置日期
     * @param scene                                场景实体
     * @param configGroup                          配置分组
     * @param roomFuncTypeRelGroupIdAndConfigGroup 空间功能类型分组ID -> 配置分组
     * @param instance                             实例
     * @param configData                           配置内容
     * @param <T>                                  配置类型
     * @return 聚合根
     * @since 7.250202.0
     */
    public static <T extends BaseDemandConfig> DemandConfigAggregate<T> of(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull String templateTag,
            @NonNull Long templateId,
            @NonNull LocalDate date,
            @Nullable DemandSceneEntity scene,
            @Nullable DemandConfigGroupEntity configGroup,
            @Nullable Map<Long, DemandConfigGroupEntity> roomFuncTypeRelGroupIdAndConfigGroup,
            @NonNull DemandInstanceValueObject instance,
            @Nullable JsonNode configData) {
        DemandConfigAggregate<T> aggregate = new DemandConfigAggregate<>();
        aggregate.setIdentifier(DemandConfigIdentifier.of(templateType, templateTag, templateId, date,
                scene, configGroup, roomFuncTypeRelGroupIdAndConfigGroup, instance));
        if (Objects.nonNull(configData)) {
            DemandConfigAnalyzeStrategy<T> configAnalyzeStrategy = templateType.demandConfigAnalyzeStrategy();
            aggregate.setConfig(configAnalyzeStrategy.analyzeConfig(configData));
        }
        return aggregate;
    }

    /**
     * 处理最终配置
     *
     * @since 5.241101.0
     */
    @SneakyThrows
    private void handleFinalConfig() {
        DemandConfigValueObject<T> finalConfig;
        InheritConfigTypeEnum inheritConfigType = getIdentifier().getInheritConfigType();
        switch (inheritConfigType) {
            case OBJECT:
                finalConfig = ObjectUtils.defaultIfNull(config, parentConfig);
                break;
            case FIELD:
                // 当上级配置为空时, 最终配置为自身配置
                if (Objects.isNull(parentConfig)) {
                    finalConfig = config;
                    break;
                }
                T parentGlobalConfig = parentConfig.getGlobalConfig();
                // 上级全局配置为空时无法继承
                if (Objects.isNull(parentGlobalConfig)) {
                    finalConfig = config;
                    break;
                }
                // 当自身配置为空时, 最终配置为上级配置
                if (Objects.isNull(config)) {
                    finalConfig = parentConfig;
                    break;
                }
                T globalConfig = config.getGlobalConfig();
                // 全局配置为空时直接赋值上级配置
                if (Objects.isNull(globalConfig)) {
                    finalConfig = DemandConfigValueObject.ofDefault(parentGlobalConfig, config.getSpaceFuncTypeConfigs());
                    break;
                }
                DemandConfigAnalyzeStrategy<T> configAnalyzeStrategy = getIdentifier()
                        .getTemplateType().demandConfigAnalyzeStrategy();
                T newFinalConfig = configAnalyzeStrategy.emptyConfig().get();
                for (String fieldName : globalConfig.cacheFieldNames()) {
                    PropertyDescriptor descriptor = globalConfig.cachePropertyDescriptor(fieldName);
                    Object value = descriptor.getReadMethod().invoke(globalConfig);
                    if (Objects.isNull(value)) {
                        value = descriptor.getReadMethod().invoke(parentGlobalConfig);
                    }
                    if (Objects.nonNull(value)) {
                        descriptor.getWriteMethod().invoke(newFinalConfig, value);
                    }
                }
                finalConfig = DemandConfigValueObject.ofDefault(newFinalConfig, null);
                break;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(
                        inheritConfigType.getClass().getSimpleName(), inheritConfigType);
        }
        this.finalConfig = finalConfig;
    }
}
