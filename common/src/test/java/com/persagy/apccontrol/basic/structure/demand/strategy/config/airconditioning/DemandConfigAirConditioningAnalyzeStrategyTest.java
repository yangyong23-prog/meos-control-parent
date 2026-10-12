package com.persagy.apccontrol.basic.structure.demand.strategy.config.airconditioning;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.config.properties.saas.children.demand.airconditioning.DemandAirconditioningDefaultSettingProperties;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfig;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigAirconditioningCustomService;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandConfigConverter;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigSaveOrUpdateParam;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningEnvironmentQualityObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueObject;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigRepository;
import com.persagy.framework.common.context.ContextUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 空调需求配置保存前处理测试。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
class DemandConfigAirConditioningAnalyzeStrategyTest {

    private DemandConfigAirconditioningCustomService customService;
    private DemandConfigRepository configRepository;
    private ObjectMapper objectMapper;
    private DemandConfigAirConditioningAnalyzeStrategy strategy;

    /**
     * 初始化独立的策略依赖和项目上下文。
     *
     * @since v7.260701.0
     */
    @BeforeEach
    void setUp() {
        this.customService = Mockito.mock(DemandConfigAirconditioningCustomService.class);
        this.configRepository = Mockito.mock(DemandConfigRepository.class);
        this.objectMapper = new ObjectMapper();
        DemandConfigConverter configConverter = Mockito.mock(DemandConfigConverter.class);
        Mockito.when(configConverter.copy(Mockito.any(DemandConfig.class)))
                .thenAnswer(invocation -> this.copyDemandConfig(invocation.getArgument(0)));
        this.strategy = Mockito.spy(new DemandConfigAirConditioningAnalyzeStrategy(
                this.objectMapper,
                Mockito.mock(DemandAirconditioningDefaultSettingProperties.class),
                configConverter,
                this.customService,
                this.configRepository));
        Mockito.doAnswer(invocation -> this.objectMapper.readValue(
                        invocation.<JsonNode>getArgument(0).toString(),
                        new TypeReference<DemandConfigValueObject<DemandConfigAirconditioningValueObject>>() {
                        }))
                .when(this.strategy).analyzeConfig(Mockito.any(JsonNode.class));
        ContextUtil.setGroupCode("group-1");
        ContextUtil.setTenant("group-1");
        ContextUtil.setProjectId("project-1");
    }

    /**
     * 清理测试线程上下文。
     *
     * @since v7.260701.0
     */
    @AfterEach
    void tearDown() {
        ContextUtil.remove();
    }

    /**
     * 同时关闭湿度和压差时，只读取一次并保持湿度、压差两次批量更新的最终内容。
     *
     * @since v7.260701.0
     */
    @Test
    void shouldReuseOneReadAndKeepTwoOrderedBatchUpdatesWhenBothQualityTypesAreDisabled() {
        List<DemandConfig> storedConfigs = Arrays.asList(
                this.newDemandConfig("space-1", this.newConfig(Boolean.TRUE, Boolean.TRUE)),
                this.newDemandConfig("space-2", this.newConfig(null, null)));
        Mockito.when(this.configRepository.listByInstances(Mockito.any()))
                .thenReturn(storedConfigs);

        DemandConfigSaveOrUpdateParam<DemandConfigAirconditioningValueObject> param =
                this.newProjectSaveParam(Boolean.FALSE, Boolean.FALSE);
        this.strategy.beforeAddConfigHandler(param);

        Mockito.verify(this.configRepository, Mockito.times(1)).listByInstances(Mockito.any());
        ArgumentCaptor<List<DemandConfig>> editCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(this.configRepository, Mockito.times(2))
                .editConfigDataWithoutEvent(editCaptor.capture());
        Assertions.assertEquals(2, editCaptor.getAllValues().get(0).size());
        Assertions.assertEquals(2, editCaptor.getAllValues().get(1).size());

        DemandConfigAirconditioningEnvironmentQualityObject humidityWriteQuality =
                this.getEnvironmentQuality(editCaptor.getAllValues().get(0).get(0));
        Assertions.assertNull(humidityWriteQuality.getIsRhQualityEnabled());
        Assertions.assertEquals(Boolean.TRUE, humidityWriteQuality.getIsPressureDiffQualityEnabled());
        DemandConfigAirconditioningEnvironmentQualityObject pressureWriteQuality =
                this.getEnvironmentQuality(editCaptor.getAllValues().get(1).get(0));
        Assertions.assertNull(pressureWriteQuality.getIsRhQualityEnabled());
        Assertions.assertNull(pressureWriteQuality.getIsPressureDiffQualityEnabled());

        DemandConfigAirconditioningEnvironmentQualityObject inputQuality =
                param.getConfigData().getGlobalConfig().getEnvironmentQuality();
        Assertions.assertNull(inputQuality.getIsRhQualityEnabled());
        Assertions.assertNull(inputQuality.getIsPressureDiffQualityEnabled());

        org.mockito.InOrder inOrder = Mockito.inOrder(this.configRepository, this.customService);
        inOrder.verify(this.configRepository).listByInstances(Mockito.any());
        inOrder.verify(this.configRepository, Mockito.times(2))
                .editConfigDataWithoutEvent(Mockito.anyList());
        inOrder.verify(this.customService).saveOrUpdateConfigData(
                Mockito.eq("project-1"), Mockito.eq(101L), Mockito.isNull(), Mockito.any());
    }

    /**
     * 已经为空但仍存在环境品质节点的配置继续按原逻辑写回数据库。
     *
     * @since v7.260701.0
     */
    @Test
    void shouldKeepOriginalWritesWhenStoredQualityFieldsAreAlreadyEmpty() {
        DemandConfigValueObject<DemandConfigAirconditioningValueObject> emptyConfig =
                this.newConfig(null, null);
        Mockito.when(this.configRepository.listByInstances(Mockito.any()))
                .thenReturn(Collections.singletonList(this.newDemandConfig("space-1", emptyConfig)));

        this.strategy.beforeAddConfigHandler(this.newProjectSaveParam(Boolean.FALSE, Boolean.FALSE));

        Mockito.verify(this.configRepository, Mockito.times(1)).listByInstances(Mockito.any());
        ArgumentCaptor<List<DemandConfig>> editCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(this.configRepository, Mockito.times(2))
                .editConfigDataWithoutEvent(editCaptor.capture());
        Assertions.assertEquals(1, editCaptor.getAllValues().get(0).size());
        Assertions.assertEquals(1, editCaptor.getAllValues().get(1).size());
    }

    /**
     * 只关闭湿度时，只执行湿度的原有查询和写入。
     *
     * @since v7.260701.0
     */
    @Test
    void shouldOnlyClearHumidityWhenHumidityIsDisabled() {
        Mockito.when(this.configRepository.listByInstances(Mockito.any()))
                .thenReturn(Collections.singletonList(
                        this.newDemandConfig("space-1", this.newConfig(Boolean.TRUE, Boolean.TRUE))));

        this.strategy.beforeAddConfigHandler(this.newProjectSaveParam(Boolean.FALSE, Boolean.TRUE));

        Mockito.verify(this.configRepository, Mockito.times(1)).listByInstances(Mockito.any());
        ArgumentCaptor<List<DemandConfig>> editCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(this.configRepository).editConfigDataWithoutEvent(editCaptor.capture());
        DemandConfigAirconditioningEnvironmentQualityObject quality =
                this.getEnvironmentQuality(editCaptor.getValue().get(0));
        Assertions.assertNull(quality.getIsRhQualityEnabled());
        Assertions.assertEquals(Boolean.TRUE, quality.getIsPressureDiffQualityEnabled());
    }

    /**
     * 只关闭压差时，只执行压差的原有查询和写入。
     *
     * @since v7.260701.0
     */
    @Test
    void shouldOnlyClearPressureDifferenceWhenPressureDifferenceIsDisabled() {
        Mockito.when(this.configRepository.listByInstances(Mockito.any()))
                .thenReturn(Collections.singletonList(
                        this.newDemandConfig("space-1", this.newConfig(Boolean.TRUE, Boolean.TRUE))));

        this.strategy.beforeAddConfigHandler(this.newProjectSaveParam(Boolean.TRUE, Boolean.FALSE));

        Mockito.verify(this.configRepository, Mockito.times(1)).listByInstances(Mockito.any());
        ArgumentCaptor<List<DemandConfig>> editCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(this.configRepository).editConfigDataWithoutEvent(editCaptor.capture());
        DemandConfigAirconditioningEnvironmentQualityObject quality =
                this.getEnvironmentQuality(editCaptor.getValue().get(0));
        Assertions.assertEquals(Boolean.TRUE, quality.getIsRhQualityEnabled());
        Assertions.assertNull(quality.getIsPressureDiffQualityEnabled());
    }

    /**
     * 创建项目级保存参数，湿度和压差开关均关闭。
     *
     * @return 保存参数
     * @since v7.260701.0
     */
    private @NonNull DemandConfigSaveOrUpdateParam<DemandConfigAirconditioningValueObject> newProjectSaveParam(
            @Nullable Boolean rhEnabled,
            @Nullable Boolean pressureDiffEnabled) {
        DemandConfigSaveOrUpdateParam<DemandConfigAirconditioningValueObject> param =
                new DemandConfigSaveOrUpdateParam<>();
        param.setTemplateId(101L);
        param.setInstanceType(DemandInstanceTypeEnum.PROJECT);
        param.setInstanceIds(Collections.singleton("project-1"));
        param.setConfigData(this.newConfig(rhEnabled, pressureDiffEnabled));
        return param;
    }

    /**
     * 获取写入参数中的环境品质配置。
     *
     * @param config 写入实体
     * @return 环境品质配置
     * @since v7.260701.0
     */
    private @NonNull DemandConfigAirconditioningEnvironmentQualityObject getEnvironmentQuality(
            @NonNull DemandConfig config) {
        return this.strategy.analyzeConfig(config.getConfigData())
                .getGlobalConfig().getEnvironmentQuality();
    }

    /**
     * 创建指定环境品质开关的配置。
     *
     * @param rhEnabled 湿度开关
     * @param pressureDiffEnabled 压差开关
     * @return 需求配置
     * @since v7.260701.0
     */
    private @NonNull DemandConfigValueObject<DemandConfigAirconditioningValueObject> newConfig(
            @Nullable Boolean rhEnabled,
            @Nullable Boolean pressureDiffEnabled) {
        DemandConfigAirconditioningEnvironmentQualityObject environmentQuality =
                new DemandConfigAirconditioningEnvironmentQualityObject();
        environmentQuality.setIsRhQualityEnabled(rhEnabled);
        environmentQuality.setIsPressureDiffQualityEnabled(pressureDiffEnabled);
        DemandConfigAirconditioningValueObject globalConfig = new DemandConfigAirconditioningValueObject();
        globalConfig.setEnvironmentQuality(environmentQuality);
        return DemandConfigValueObject.ofDefault(globalConfig, Collections.emptyList());
    }

    /**
     * 创建一条已持久化需求配置。
     *
     * @param instanceId 实例ID
     * @return 需求配置实体
     * @since v7.260701.0
     */
    private @NonNull DemandConfig newDemandConfig(
            @NonNull String instanceId,
            @NonNull DemandConfigValueObject<DemandConfigAirconditioningValueObject> configData) {
        DemandConfig config = new DemandConfig();
        config.setRelTemplateId(101L);
        config.setDemandInstanceType(DemandInstanceTypeEnum.SPACE);
        config.setDemandInstanceId(instanceId);
        config.setConfigData(this.objectMapper.valueToTree(configData));
        return config;
    }

    /**
     * 复制清理前的实体元数据。
     *
     * @param source 原实体
     * @return 实体副本
     * @since v7.260701.0
     */
    private @NonNull DemandConfig copyDemandConfig(@NonNull DemandConfig source) {
        DemandConfig copy = new DemandConfig();
        copy.setId(source.getId());
        copy.setProjectId(source.getProjectId());
        copy.setRelTemplateId(source.getRelTemplateId());
        copy.setRelSceneId(source.getRelSceneId());
        copy.setRelGroupId(source.getRelGroupId());
        copy.setDemandInstanceType(source.getDemandInstanceType());
        copy.setDemandInstanceId(source.getDemandInstanceId());
        copy.setConfigData(source.getConfigData());
        return copy;
    }
}
