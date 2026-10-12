package com.persagy.apccontrol.basic.service.demand.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.executor.log.LogComponentProvider;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigResult;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.param.demand.DemandConfigResultBatchQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.DemandConfigResultVO;
import com.persagy.apccontrol.basic.model.vo.demand.DemandInstanceTagVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateDateSettingVO;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.MidPermissionClientService;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.service.demand.DemandCalendarQueryService;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandConfigConverter;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandConverter;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGatewayImpl;
import com.persagy.apccontrol.basic.structure.demand.gateway.instance.DemandInstanceGateway;
import com.persagy.apccontrol.basic.structure.demand.lifecycle.config.DemandConfigAirconditioningQueryEnricher;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.dto.DemandInstanceCustomizedTagDTO;
import com.persagy.apccontrol.basic.structure.demand.model.instance.domain.DemandInstanceConfigEntity;
import com.persagy.apccontrol.basic.structure.demand.model.instance.domain.identifier.DemandInstanceConfigIdentifier;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueSelectValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningWorkConditionValueObject;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigGroupRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigResultRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandInstanceRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandTemplateRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigResultRepositoryQueryParam;
import com.persagy.apccontrol.basic.structure.demand.repository.scene.DemandSceneRepository;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandStrategy;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.airconditioning.DemandConfigAirConditioningAnalyzeStrategy;
import com.persagy.framework.common.i18n.I18nProperties;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Demand-config result query compatibility tests.
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class DemandQueryServiceImplTest {

    private static final Long TEMPLATE_ID = 101L;
    private static final Long TAG_TEMPLATE_ID = 102L;
    private static final Long DATE_TEMPLATE_ID = 103L;
    private static final Long RESOLVED_TEMPLATE_ID = 104L;
    private static final Long SCENE_ID = 202L;
    private static final String TEMPLATE_TAG = "template-tag";
    private static final LocalDate HISTORY_DATE = LocalDate.of(2026, 1, 15);

    private static final DemandCalendarQueryService DEMAND_CALENDAR_QUERY_SERVICE =
            Mockito.mock(DemandCalendarQueryService.class);
    private static final DemandTemplateRepository DEMAND_TEMPLATE_REPOSITORY =
            Mockito.mock(DemandTemplateRepository.class);
    private static final DemandStrategy DEMAND_STRATEGY = Mockito.mock(DemandStrategy.class);

    /**
     * Air-conditioning config parser used by result JSON conversion tests.
     *
     * @since v7.260701.0
     */
    private static final DemandConfigAirConditioningAnalyzeStrategy DEMAND_CONFIG_ANALYZE_STRATEGY =
            Mockito.mock(DemandConfigAirConditioningAnalyzeStrategy.class);

    private static @Nullable ApplicationContext originalApplicationContext;
    private static @Nullable StaticApplicationContext testApplicationContext;

    private DtpRwdClientService dtpRwdClientService;
    private DemandConfigGatewayImpl<BaseDemandConfig> demandConfigGateway;
    private DemandConfigResultRepository demandConfigResultRepository;
    private DemandInstanceGateway demandInstanceGateway;
    private DemandConfigAirconditioningQueryEnricher demandConfigAirconditioningQueryEnricher;
    private DemandQueryServiceImpl service;

    /**
     * Register isolated template-resolution collaborators used by {@link SpringUtil}.
     *
     * @since v7.260701.0
     */
    @BeforeAll
    public static void setUpSpringUtil() {
        originalApplicationContext = SpringUtil.getApplicationContext();
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.getBeanFactory().registerSingleton(
                "demandCalendarQueryService", DEMAND_CALENDAR_QUERY_SERVICE);
        applicationContext.getBeanFactory().registerSingleton(
                "demandTemplateRepository", DEMAND_TEMPLATE_REPOSITORY);
        applicationContext.getBeanFactory().registerSingleton(
                "demandAirconditioningStrategyImpl", DEMAND_STRATEGY);
        applicationContext.getBeanFactory().registerSingleton(
                DemandConfigAirConditioningAnalyzeStrategy.BEAN_NAME,
                DemandQueryServiceImplTest.DEMAND_CONFIG_ANALYZE_STRATEGY);
        I18nProperties i18nProperties = Mockito.mock(I18nProperties.class);
        Mockito.when(i18nProperties.getI18n()).thenReturn("zh-CN");
        applicationContext.getBeanFactory().registerSingleton(
                "i18nProperties", i18nProperties);
        applicationContext.refresh();
        testApplicationContext = applicationContext;
        new SpringUtil().setApplicationContext(applicationContext);
    }

    /**
     * Restore the original {@link SpringUtil} context.
     *
     * @since v7.260701.0
     */
    @AfterAll
    public static void tearDownSpringUtil() {
        new SpringUtil().setApplicationContext(originalApplicationContext);
        if (testApplicationContext != null) {
            testApplicationContext.close();
        }
        testApplicationContext = null;
        originalApplicationContext = null;
    }

    /**
     * Initialize isolated collaborators.
     *
     * @since v7.260701.0
     */
    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() {
        Mockito.reset(DEMAND_CALENDAR_QUERY_SERVICE, DEMAND_TEMPLATE_REPOSITORY);
        Mockito.reset(
                DemandQueryServiceImplTest.DEMAND_STRATEGY,
                DemandQueryServiceImplTest.DEMAND_CONFIG_ANALYZE_STRATEGY);
        Mockito.when(DemandQueryServiceImplTest.DEMAND_STRATEGY.mapDemandInstanceCustomizedTagNameAndValue(
                        Mockito.any(DemandTemplateTypeEnum.class), Mockito.anySet()))
                .thenReturn(Collections.emptyList());
        DemandTemplate template = this.buildTemplate(TEMPLATE_ID);
        Mockito.when(DEMAND_TEMPLATE_REPOSITORY.getByTemplateIdAndDate(
                        Mockito.eq(false),
                        Mockito.eq(TEMPLATE_ID),
                        Mockito.nullable(LocalDate.class)))
                .thenReturn(template);
        this.dtpRwdClientService = Mockito.mock(DtpRwdClientService.class);
        this.demandConfigResultRepository = Mockito.mock(DemandConfigResultRepository.class);
        this.demandInstanceGateway = Mockito.mock(DemandInstanceGateway.class);
        this.demandConfigAirconditioningQueryEnricher =
                Mockito.mock(DemandConfigAirconditioningQueryEnricher.class);
        Mockito.when(this.demandInstanceGateway.listInstanceConfigs(
                        Mockito.any())).thenReturn(Collections.emptyList());
        this.demandConfigGateway = Mockito.spy(new DemandConfigGatewayImpl<BaseDemandConfig>(
                this.dtpRwdClientService,
                Mockito.mock(DemandConfigConverter.class),
                DEMAND_TEMPLATE_REPOSITORY,
                Mockito.mock(DemandSceneRepository.class),
                Mockito.mock(DemandInstanceRepository.class),
                Mockito.mock(DemandConfigRepository.class),
                Mockito.mock(DemandConfigGroupRepository.class),
                Mockito.mock(InstanceService.class),
                Collections.emptyList(),
                this.demandConfigResultRepository,
                this.demandInstanceGateway,
                this.demandConfigAirconditioningQueryEnricher));
        this.service = new DemandQueryServiceImpl(
                Mockito.mock(DemandConverter.class),
                this.dtpRwdClientService,
                Mockito.mock(MidPermissionClientService.class),
                DEMAND_CALENDAR_QUERY_SERVICE,
                DEMAND_TEMPLATE_REPOSITORY,
                this.demandConfigGateway,
                this.demandInstanceGateway,
                Mockito.mock(DtpKnowledgeClientService.class));
    }

    /**
     * Verify template ID takes priority and the resolved template ID drives result lookup.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldResolveTemplateIdBeforeQueryingResults() {
        DemandTemplate template = this.buildTemplate(RESOLVED_TEMPLATE_ID);
        Mockito.when(DEMAND_TEMPLATE_REPOSITORY.getByTemplateIdAndDate(
                        false, TEMPLATE_ID, HISTORY_DATE))
                .thenReturn(template);
        Mockito.when(this.demandConfigResultRepository.list(
                        Mockito.any(DemandConfigResultRepositoryQueryParam.class)))
                .thenReturn(Collections.emptyList());
        DemandConfigResultBatchQueryParam param = new DemandConfigResultBatchQueryParam();
        param.setTemplateId(TEMPLATE_ID);
        param.setInstanceType(DemandInstanceTypeEnum.SPACE);
        param.setTemplateTag(TEMPLATE_TAG);
        param.setDate(HISTORY_DATE);

        Assertions.assertTrue(this.service.listConfigResultsByInstances(param).isEmpty());

        Mockito.verify(this.demandConfigGateway, Mockito.times(1))
                .listConfigResultsByInstances(param);
        ArgumentCaptor<DemandConfigResultRepositoryQueryParam> resultParamCaptor =
                ArgumentCaptor.forClass(DemandConfigResultRepositoryQueryParam.class);
        Mockito.verify(this.demandConfigResultRepository).list(resultParamCaptor.capture());
        Assertions.assertEquals(
                RESOLVED_TEMPLATE_ID,
                resultParamCaptor.getValue().getRelTemplateId());
        Mockito.verify(DEMAND_TEMPLATE_REPOSITORY).getByTemplateIdAndDate(
                false, TEMPLATE_ID, HISTORY_DATE);
        Mockito.verify(this.demandConfigAirconditioningQueryEnricher).enrichResults(
                Mockito.eq(Collections.emptyList()),
                Mockito.eq(DemandTemplateTypeEnum.AIRCONDITIONING),
                Mockito.eq(DemandQueryServiceImplTest.RESOLVED_TEMPLATE_ID),
                Mockito.eq(DemandInstanceTypeEnum.SPACE),
                Mockito.eq(DemandQueryServiceImplTest.HISTORY_DATE));
        Mockito.verifyNoInteractions(DEMAND_CALENDAR_QUERY_SERVICE);
    }

    /**
     * Verify template tag takes priority over date when template ID is absent.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldResolveTemplateTagBeforeDate() {
        DemandTemplate template = this.buildTemplate(TAG_TEMPLATE_ID);
        Mockito.when(DEMAND_TEMPLATE_REPOSITORY.getByTemplateByTemplateTypeTag(
                        false, DemandTemplateTypeEnum.AIRCONDITIONING, TEMPLATE_TAG))
                .thenReturn(template);
        Mockito.when(this.demandConfigResultRepository.list(
                        Mockito.any(DemandConfigResultRepositoryQueryParam.class)))
                .thenReturn(Collections.emptyList());
        DemandConfigResultBatchQueryParam param = new DemandConfigResultBatchQueryParam();
        param.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
        param.setInstanceType(DemandInstanceTypeEnum.SPACE);
        param.setTemplateTag(TEMPLATE_TAG);
        param.setDate(HISTORY_DATE);

        Assertions.assertTrue(this.service.listConfigResultsByInstances(param).isEmpty());

        ArgumentCaptor<DemandConfigResultRepositoryQueryParam> resultParamCaptor =
                ArgumentCaptor.forClass(DemandConfigResultRepositoryQueryParam.class);
        Mockito.verify(this.demandConfigResultRepository).list(resultParamCaptor.capture());
        Assertions.assertEquals(TAG_TEMPLATE_ID, resultParamCaptor.getValue().getRelTemplateId());
        Mockito.verifyNoInteractions(DEMAND_CALENDAR_QUERY_SERVICE);
    }

    /**
     * Verify business date resolves the calendar template before result lookup.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldResolveTemplateByDate() {
        DemandTemplateDateSettingVO dateSetting =
                Mockito.mock(DemandTemplateDateSettingVO.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(dateSetting.getRelTemplate().getTemplateId()).thenReturn(DATE_TEMPLATE_ID);
        Mockito.when(DEMAND_CALENDAR_QUERY_SERVICE.getDateSetting(
                        DemandTemplateTypeEnum.AIRCONDITIONING, HISTORY_DATE))
                .thenReturn(dateSetting);
        DemandTemplate template = this.buildTemplate(DATE_TEMPLATE_ID);
        Mockito.when(DEMAND_TEMPLATE_REPOSITORY.getByTemplateIdAndDate(
                        false, DATE_TEMPLATE_ID, HISTORY_DATE))
                .thenReturn(template);
        Mockito.when(this.demandConfigResultRepository.list(
                        Mockito.any(DemandConfigResultRepositoryQueryParam.class)))
                .thenReturn(Collections.emptyList());
        DemandConfigResultBatchQueryParam param = new DemandConfigResultBatchQueryParam();
        param.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
        param.setInstanceType(DemandInstanceTypeEnum.SPACE);
        param.setDate(HISTORY_DATE);

        Assertions.assertTrue(this.service.listConfigResultsByInstances(param).isEmpty());

        ArgumentCaptor<DemandConfigResultRepositoryQueryParam> resultParamCaptor =
                ArgumentCaptor.forClass(DemandConfigResultRepositoryQueryParam.class);
        Mockito.verify(this.demandConfigResultRepository).list(resultParamCaptor.capture());
        Assertions.assertEquals(DATE_TEMPLATE_ID, resultParamCaptor.getValue().getRelTemplateId());
    }

    /**
     * Verify a missing template is logged and treated as an empty result.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldLogAndReturnEmptyWhenTemplateIsMissing() {
        Mockito.when(DEMAND_TEMPLATE_REPOSITORY.getByTemplateIdAndDate(
                        Mockito.eq(false),
                        Mockito.eq(TEMPLATE_ID),
                        Mockito.nullable(LocalDate.class)))
                .thenReturn(null);
        DemandConfigResultBatchQueryParam param = new DemandConfigResultBatchQueryParam();
        param.setTemplateId(TEMPLATE_ID);
        param.setInstanceType(DemandInstanceTypeEnum.SPACE);
        LogComponentProvider logProvider = Mockito.mock(LogComponentProvider.class);
        Mockito.doReturn(logProvider).when(this.demandConfigGateway).logProjectInstance();

        Assertions.assertTrue(this.service.listConfigResultsByInstances(param).isEmpty());

        Mockito.verify(logProvider).warn(
                Mockito.eq("未查询到需求模板，返回空的需求配置结果"),
                Mockito.<List<Pair<String, Object>>>any());
        Mockito.verifyNoInteractions(
                this.demandConfigResultRepository,
                this.dtpRwdClientService);
    }

    /**
     * Verify result filters propagate directly without realtime or RWD fallback.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldPropagateResultFiltersWithoutFallback() {
        Set<String> requestedInstanceIds =
                new LinkedHashSet<>(Arrays.asList("space-1", "space-2"));
        Mockito.when(this.demandConfigResultRepository.list(
                        Mockito.any(DemandConfigResultRepositoryQueryParam.class)))
                .thenReturn(Collections.emptyList());

        DemandConfigResultBatchQueryParam param = new DemandConfigResultBatchQueryParam();
        param.setDate(HISTORY_DATE);
        param.setTemplateId(TEMPLATE_ID);
        param.setSceneId(SCENE_ID);
        param.setInstanceType(DemandInstanceTypeEnum.SPACE);
        param.setInstanceIds(requestedInstanceIds);
        param.setAutoFillEmptyInstanceIds(true);
        param.setFilterShieldedSpaces(true);
        param.setFillInstanceName(true);

        Assertions.assertTrue(this.service.listConfigResultsByInstances(param).isEmpty());

        ArgumentCaptor<DemandConfigResultRepositoryQueryParam> resultParamCaptor =
                ArgumentCaptor.forClass(DemandConfigResultRepositoryQueryParam.class);
        Mockito.verify(this.demandConfigResultRepository).list(resultParamCaptor.capture());
        DemandConfigResultRepositoryQueryParam actualResultParam = resultParamCaptor.getValue();
        Assertions.assertEquals(HISTORY_DATE, actualResultParam.getDate());
        Assertions.assertEquals(TEMPLATE_ID, actualResultParam.getRelTemplateId());
        Assertions.assertEquals(SCENE_ID, actualResultParam.getRelSceneId());
        Assertions.assertEquals(Boolean.FALSE, actualResultParam.getIsShielded());
        Assertions.assertEquals(
                DemandInstanceTypeEnum.SPACE,
                actualResultParam.getDemandInstanceType());
        Assertions.assertEquals(
                requestedInstanceIds,
                actualResultParam.getDemandInstanceIds());
        Assertions.assertEquals(4, actualResultParam.getSelectColumns().size());
        Mockito.verifyNoInteractions(this.dtpRwdClientService);
    }

    /**
     * Verify omitted shield filtering defaults to excluding shielded objects of the requested type.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldDefaultToFilteringShieldedObjectsForTypedQuery() {
        Mockito.when(this.demandConfigResultRepository.list(
                        Mockito.any(DemandConfigResultRepositoryQueryParam.class)))
                .thenReturn(Collections.emptyList());
        DemandConfigResultBatchQueryParam param = new DemandConfigResultBatchQueryParam();
        param.setTemplateId(TEMPLATE_ID);
        param.setInstanceType(DemandInstanceTypeEnum.SPACE);
        Assertions.assertTrue(param.isFilterShieldedSpaces());
        Assertions.assertFalse(param.isFillInstanceName());

        Assertions.assertTrue(this.service.listConfigResultsByInstances(param).isEmpty());

        ArgumentCaptor<DemandConfigResultRepositoryQueryParam> resultParamCaptor =
                ArgumentCaptor.forClass(DemandConfigResultRepositoryQueryParam.class);
        Mockito.verify(this.demandConfigResultRepository).list(resultParamCaptor.capture());
        DemandConfigResultRepositoryQueryParam actualResultParam = resultParamCaptor.getValue();
        Assertions.assertNull(actualResultParam.getRelSceneId());
        Assertions.assertEquals(DemandInstanceTypeEnum.SPACE, actualResultParam.getDemandInstanceType());
        Assertions.assertNull(actualResultParam.getDemandInstanceIds());
        Assertions.assertEquals(Boolean.FALSE, actualResultParam.getIsShielded());
        Mockito.verifyNoInteractions(
                this.dtpRwdClientService);
    }

    /**
     * Verify callers can include shielded objects and receive their shielded state.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldReturnShieldedStateWhenShieldFilteringIsDisabled() {
        DemandConfigResult result = new DemandConfigResult();
        result.setDemandInstanceId("space-1");
        result.setIsShielded(Boolean.TRUE);
        Mockito.when(this.demandConfigResultRepository.list(
                        Mockito.any(DemandConfigResultRepositoryQueryParam.class)))
                .thenReturn(Collections.singletonList(result));
        DemandConfigResultBatchQueryParam param = new DemandConfigResultBatchQueryParam();
        param.setTemplateId(TEMPLATE_ID);
        param.setInstanceType(DemandInstanceTypeEnum.SPACE);
        param.setFilterShieldedSpaces(false);

        List<DemandConfigResultVO<BaseDemandConfig>> results =
                this.service.listConfigResultsByInstances(param);

        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("space-1", results.get(0).getInstanceId());
        Assertions.assertTrue(results.get(0).isShielded());
        Assertions.assertNull(results.get(0).getInstanceTag());
        ArgumentCaptor<DemandConfigResultRepositoryQueryParam> resultParamCaptor =
                ArgumentCaptor.forClass(DemandConfigResultRepositoryQueryParam.class);
        Mockito.verify(this.demandConfigResultRepository).list(resultParamCaptor.capture());
        Assertions.assertNull(resultParamCaptor.getValue().getIsShielded());
        Assertions.assertEquals(4, resultParamCaptor.getValue().getSelectColumns().size());
        Mockito.verifyNoInteractions(this.dtpRwdClientService);
        Mockito.verifyNoInteractions(DEMAND_STRATEGY);
    }

    /**
     * Verify instance tags are queried in batch and returned only when requested.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldQueryAndReturnInstanceTagWhenRequested() {
        DemandConfigResult result = new DemandConfigResult();
        result.setDemandTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
        result.setDemandInstanceType(DemandInstanceTypeEnum.SPACE);
        result.setDemandInstanceId("space-1");
        result.setIsCustomizedConfig(Boolean.TRUE);
        result.setIsShielded(Boolean.TRUE);
        DemandInstanceValueObject instance =
                new DemandInstanceValueObject(DemandInstanceTypeEnum.SPACE, "space-1");
        DemandInstanceCustomizedTagDTO customizedTag = new DemandInstanceCustomizedTagDTO()
                .setInstance(instance)
                .setCustomizedTag(Collections.<String, Object>singletonMap("tag", "value"));
        Mockito.when(DEMAND_STRATEGY.mapDemandInstanceCustomizedTagNameAndValue(
                        Mockito.eq(DemandTemplateTypeEnum.AIRCONDITIONING), Mockito.anySet()))
                .thenReturn(Collections.singletonList(customizedTag));
        Mockito.when(this.demandConfigResultRepository.list(
                        Mockito.any(DemandConfigResultRepositoryQueryParam.class)))
                .thenReturn(Collections.singletonList(result));
        DemandConfigResultBatchQueryParam param = new DemandConfigResultBatchQueryParam();
        param.setTemplateId(TEMPLATE_ID);
        param.setInstanceType(DemandInstanceTypeEnum.SPACE);
        param.setFilterShieldedSpaces(false);
        param.setQueryInstanceTag(true);

        List<DemandConfigResultVO<BaseDemandConfig>> results =
                this.service.listConfigResultsByInstances(param);

        Assertions.assertEquals(1, results.size());
        DemandInstanceTagVO actualTag = results.get(0).getInstanceTag();
        Assertions.assertNotNull(actualTag);
        Assertions.assertTrue(actualTag.getCustomConfig());
        Assertions.assertFalse(actualTag.getExistCfg());
        Assertions.assertTrue(actualTag.getShielded());
        Assertions.assertFalse(actualTag.getInBlacklisted());
        Assertions.assertEquals(
                Collections.singletonMap("tag", "value"), actualTag.getCustomizedTag());
        ArgumentCaptor<DemandConfigResultRepositoryQueryParam> resultParamCaptor =
                ArgumentCaptor.forClass(DemandConfigResultRepositoryQueryParam.class);
        Mockito.verify(this.demandConfigResultRepository).list(resultParamCaptor.capture());
        Assertions.assertEquals(6, resultParamCaptor.getValue().getSelectColumns().size());
        Mockito.verify(DEMAND_STRATEGY).mapDemandInstanceCustomizedTagNameAndValue(
                Mockito.eq(DemandTemplateTypeEnum.AIRCONDITIONING), Mockito.anySet());
    }

    /**
     * Verify project query-time work-condition enrichment does not change the raw demand-config
     * customization flag exposed through the instance tag.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldKeepProjectCustomConfigTagBoundToStableResultFlag() {
        DemandConfigResult result = new DemandConfigResult();
        result.setDemandTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
        result.setDemandInstanceType(DemandInstanceTypeEnum.PROJECT);
        result.setDemandInstanceId("project-id");
        result.setIsCustomizedConfig(Boolean.FALSE);
        result.setIsShielded(Boolean.FALSE);
        result.setFinalConfigJson(new ObjectMapper().createObjectNode());
        DemandConfigAirconditioningValueObject stableGlobalConfig =
                new DemandConfigAirconditioningValueObject();
        DemandConfigValueObject<DemandConfigAirconditioningValueObject> stableFinalConfig =
                DemandConfigValueObject.ofDefault(stableGlobalConfig, null);
        Mockito.when(DemandQueryServiceImplTest.DEMAND_CONFIG_ANALYZE_STRATEGY.analyzeConfig(Mockito.any()))
                .thenReturn(stableFinalConfig);
        Mockito.when(this.demandConfigResultRepository.list(
                        Mockito.any(DemandConfigResultRepositoryQueryParam.class)))
                .thenReturn(Collections.singletonList(result));
        Mockito.doAnswer(invocation -> {
            List<DemandConfigResultVO<BaseDemandConfig>> resultVOs = invocation.getArgument(0);
            DemandConfigAirconditioningValueObject globalConfig =
                    (DemandConfigAirconditioningValueObject) resultVOs.get(0)
                            .getFinalConfig().getGlobalConfig();
            globalConfig.setValueSelect(new DemandConfigAirconditioningValueSelectValueObject(
                    BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_ENERGY));
            globalConfig.setWorkCondition(new DemandConfigAirconditioningWorkConditionValueObject());
            return null;
        }).when(this.demandConfigAirconditioningQueryEnricher).enrichResults(
                Mockito.anyList(),
                Mockito.eq(DemandTemplateTypeEnum.AIRCONDITIONING),
                Mockito.eq(DemandQueryServiceImplTest.TEMPLATE_ID),
                Mockito.eq(DemandInstanceTypeEnum.PROJECT),
                Mockito.isNull());
        DemandConfigResultBatchQueryParam param = new DemandConfigResultBatchQueryParam();
        param.setTemplateId(DemandQueryServiceImplTest.TEMPLATE_ID);
        param.setInstanceType(DemandInstanceTypeEnum.PROJECT);
        param.setInstanceIds(Collections.singleton("project-id"));
        param.setQueryInstanceTag(true);

        List<DemandConfigResultVO<BaseDemandConfig>> results =
                this.service.listConfigResultsByInstances(param);

        Assertions.assertEquals(1, results.size());
        DemandConfigResultVO<BaseDemandConfig> actualResult = results.get(0);
        Assertions.assertNotNull(actualResult.getInstanceTag());
        Assertions.assertFalse(actualResult.getInstanceTag().getCustomConfig());
        Assertions.assertTrue(actualResult.getInstanceTag().getExistCfg());
        DemandConfigAirconditioningValueObject actualGlobalConfig =
                (DemandConfigAirconditioningValueObject) actualResult.getFinalConfig().getGlobalConfig();
        Assertions.assertNotNull(actualGlobalConfig.getValueSelect());
        Assertions.assertNotNull(actualGlobalConfig.getWorkCondition());
    }

    /**
     * Verify a blacklisted input object is returned with its instance tag even without a result row.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldReturnBlacklistedInstanceTagWithoutResultRow() {
        DemandInstanceConfigEntity blacklistedConfig = this.buildInstanceConfigEntity(
                "blacklisted-space", DemandInstanceTypeEnum.SPACE, true, false);
        Mockito.when(this.demandConfigResultRepository.list(
                        Mockito.any(DemandConfigResultRepositoryQueryParam.class)))
                .thenReturn(Collections.emptyList());
        Mockito.when(this.demandInstanceGateway.listInstanceConfigs(
                        Mockito.any())).thenReturn(Collections.singletonList(blacklistedConfig));
        DemandConfigResultBatchQueryParam param = new DemandConfigResultBatchQueryParam();
        param.setTemplateId(DemandQueryServiceImplTest.TEMPLATE_ID);
        param.setInstanceType(DemandInstanceTypeEnum.SPACE);
        param.setInstanceIds(Collections.singleton("blacklisted-space"));
        param.setQueryInstanceTag(true);

        List<DemandConfigResultVO<BaseDemandConfig>> results =
                this.service.listConfigResultsByInstances(param);

        Assertions.assertEquals(1, results.size());
        DemandConfigResultVO<BaseDemandConfig> result = results.get(0);
        Assertions.assertEquals("blacklisted-space", result.getInstanceId());
        Assertions.assertNull(result.getFinalConfig());
        Assertions.assertNotNull(result.getInstanceTag());
        Assertions.assertTrue(result.getInstanceTag().getInBlacklisted());
        Assertions.assertFalse(result.getInstanceTag().getShielded());
        Mockito.verify(this.demandInstanceGateway).listInstanceConfigs(Mockito.argThat(queryParam ->
                DemandTemplateTypeEnum.AIRCONDITIONING.equals(queryParam.getTemplateType())
                        && DemandInstanceTypeEnum.SPACE.equals(queryParam.getInstanceType())
                        && Collections.singleton("blacklisted-space").equals(queryParam.getInstanceIds())));
    }

    /**
     * Verify the default shield filter also applies to a blacklisted object appended from instance configuration.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldFilterShieldedBlacklistedInstanceWithoutResultRow() {
        DemandInstanceConfigEntity blacklistedConfig = this.buildInstanceConfigEntity(
                "shielded-blacklisted-space", DemandInstanceTypeEnum.SPACE, true, true);
        Mockito.when(this.demandInstanceGateway.listInstanceConfigs(
                        Mockito.any())).thenReturn(Collections.singletonList(blacklistedConfig));
        Mockito.when(this.demandConfigResultRepository.list(
                        Mockito.any(DemandConfigResultRepositoryQueryParam.class)))
                .thenReturn(Collections.emptyList());
        DemandConfigResultBatchQueryParam param = new DemandConfigResultBatchQueryParam();
        param.setTemplateId(DemandQueryServiceImplTest.TEMPLATE_ID);
        param.setInstanceType(DemandInstanceTypeEnum.SPACE);
        param.setInstanceIds(Collections.singleton("shielded-blacklisted-space"));
        param.setQueryInstanceTag(true);

        Assertions.assertTrue(this.service.listConfigResultsByInstances(param).isEmpty());
        Mockito.verify(DemandQueryServiceImplTest.DEMAND_STRATEGY).mapDemandInstanceCustomizedTagNameAndValue(
                Mockito.eq(DemandTemplateTypeEnum.AIRCONDITIONING),
                Mockito.argThat(instances -> instances.contains(new DemandInstanceValueObject(
                        DemandInstanceTypeEnum.SPACE, "shielded-blacklisted-space"))));
    }

    /**
     * Verify a typed missing-ID scope keeps all-of-type semantics without RWD auto-fill.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldPreserveTypedMissingInstanceIdsWithoutRwdQuery() {
        Mockito.when(this.demandConfigResultRepository.list(
                        Mockito.any(DemandConfigResultRepositoryQueryParam.class)))
                .thenReturn(Collections.emptyList());
        DemandConfigResultBatchQueryParam param = new DemandConfigResultBatchQueryParam();
        param.setTemplateId(TEMPLATE_ID);
        param.setInstanceType(DemandInstanceTypeEnum.SPACE);
        param.setAutoFillEmptyInstanceIds(true);

        Assertions.assertTrue(this.service.listConfigResultsByInstances(param).isEmpty());

        ArgumentCaptor<DemandConfigResultRepositoryQueryParam> resultParamCaptor =
                ArgumentCaptor.forClass(DemandConfigResultRepositoryQueryParam.class);
        Mockito.verify(this.demandConfigResultRepository).list(resultParamCaptor.capture());
        Assertions.assertEquals(
                DemandInstanceTypeEnum.SPACE,
                resultParamCaptor.getValue().getDemandInstanceType());
        Assertions.assertNull(resultParamCaptor.getValue().getDemandInstanceIds());
        Mockito.verifyNoInteractions(
                this.dtpRwdClientService);
    }

    /**
     * Verify a typed empty-ID scope keeps all-of-type semantics without RWD auto-fill.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldPreserveTypedEmptyInstanceIdsWithoutRwdQuery() {
        Mockito.when(this.demandConfigResultRepository.list(
                        Mockito.any(DemandConfigResultRepositoryQueryParam.class)))
                .thenReturn(Collections.emptyList());
        DemandConfigResultBatchQueryParam param = new DemandConfigResultBatchQueryParam();
        param.setTemplateId(TEMPLATE_ID);
        param.setInstanceType(DemandInstanceTypeEnum.SPACE);
        param.setInstanceIds(Collections.emptySet());
        param.setAutoFillEmptyInstanceIds(true);

        Assertions.assertTrue(this.service.listConfigResultsByInstances(param).isEmpty());

        ArgumentCaptor<DemandConfigResultRepositoryQueryParam> resultParamCaptor =
                ArgumentCaptor.forClass(DemandConfigResultRepositoryQueryParam.class);
        Mockito.verify(this.demandConfigResultRepository).list(resultParamCaptor.capture());
        Assertions.assertEquals(
                DemandInstanceTypeEnum.SPACE,
                resultParamCaptor.getValue().getDemandInstanceType());
        Assertions.assertEquals(
                Collections.emptySet(),
                resultParamCaptor.getValue().getDemandInstanceIds());
        Mockito.verifyNoInteractions(
                this.dtpRwdClientService);
    }

    /**
     * Verify null elements are filtered through a copied collection before explicit-ID lookup.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldFilterNullInstanceIdsWithoutMutatingCallerCollection() throws Exception {
        Mockito.when(this.demandConfigResultRepository.list(
                        Mockito.any(DemandConfigResultRepositoryQueryParam.class)))
                .thenReturn(Collections.emptyList());
        DemandConfigResultBatchQueryParam param = new ObjectMapper().readValue(
                "{\"templateId\":101,\"instanceType\":\"SPACE\","
                        + "\"instanceIds\":[\"space-1\",null]}",
                DemandConfigResultBatchQueryParam.class);
        @Nullable Set<@Nullable String> deserializedInstanceIds = param.getInstanceIds();
        if (deserializedInstanceIds == null) {
            Assertions.fail("Deserialized instance IDs must not be null");
            return;
        }
        Set<@Nullable String> immutableInstanceIds =
                Collections.unmodifiableSet(deserializedInstanceIds);
        param.setInstanceIds(immutableInstanceIds);

        Assertions.assertTrue(this.service.listConfigResultsByInstances(param).isEmpty());

        Assertions.assertEquals(2, immutableInstanceIds.size());
        Assertions.assertNotSame(immutableInstanceIds, param.getInstanceIds());
        Assertions.assertEquals(Collections.singleton("space-1"), param.getInstanceIds());
        ArgumentCaptor<DemandConfigResultRepositoryQueryParam> resultParamCaptor =
                ArgumentCaptor.forClass(DemandConfigResultRepositoryQueryParam.class);
        Mockito.verify(this.demandConfigResultRepository).list(resultParamCaptor.capture());
        Assertions.assertEquals(
                DemandInstanceTypeEnum.SPACE,
                resultParamCaptor.getValue().getDemandInstanceType());
        Assertions.assertEquals(
                Collections.singleton("space-1"),
                resultParamCaptor.getValue().getDemandInstanceIds());
        Mockito.verifyNoInteractions(
                this.dtpRwdClientService);
    }

    /**
     * Verify null ID cleanup keeps the empty collection while Bean Validation rejects a missing instance type.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldRejectNullOnlyInstanceIdsWithoutInstanceType() throws Exception {
        DemandConfigResultBatchQueryParam param = new ObjectMapper().readValue(
                "{\"templateId\":101,\"instanceIds\":[null]}",
                DemandConfigResultBatchQueryParam.class);

        param.checkAndSet();
        Set<ConstraintViolation<DemandConfigResultBatchQueryParam>> violations =
                Validation.buildDefaultValidatorFactory().getValidator().validate(param);
        Assertions.assertTrue(violations.stream()
                .anyMatch(violation -> "instanceType".equals(violation.getPropertyPath().toString())));
        Assertions.assertEquals(Collections.emptySet(), param.getInstanceIds());
        Mockito.verifyNoInteractions(
                this.demandConfigResultRepository,
                this.dtpRwdClientService);
    }

    /**
     * Verify a typed null-only instance ID collection becomes all-of-type scope.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldTreatTypedNullOnlyInstanceIdsAsAllOfType() throws Exception {
        Mockito.when(this.demandConfigResultRepository.list(
                        Mockito.any(DemandConfigResultRepositoryQueryParam.class)))
                .thenReturn(Collections.emptyList());
        DemandConfigResultBatchQueryParam param = new ObjectMapper().readValue(
                "{\"templateId\":101,\"instanceType\":\"SPACE\",\"instanceIds\":[null]}",
                DemandConfigResultBatchQueryParam.class);

        Assertions.assertTrue(this.service.listConfigResultsByInstances(param).isEmpty());

        ArgumentCaptor<DemandConfigResultRepositoryQueryParam> resultParamCaptor =
                ArgumentCaptor.forClass(DemandConfigResultRepositoryQueryParam.class);
        Mockito.verify(this.demandConfigResultRepository).list(resultParamCaptor.capture());
        Assertions.assertEquals(
                DemandInstanceTypeEnum.SPACE,
                resultParamCaptor.getValue().getDemandInstanceType());
        Assertions.assertEquals(
                Collections.emptySet(),
                resultParamCaptor.getValue().getDemandInstanceIds());
        Assertions.assertEquals(Collections.emptySet(), param.getInstanceIds());
        Mockito.verifyNoInteractions(
                this.dtpRwdClientService);
    }

    /**
     * Build one resolved demand template.
     *
     * @param templateId template ID
     * @return demand template
     * @since v7.260701.0
     */
    private @NonNull DemandTemplate buildTemplate(@NonNull Long templateId) {
        DemandTemplate template = Mockito.mock(DemandTemplate.class);
        Mockito.when(template.getId()).thenReturn(templateId);
        Mockito.when(template.getDemandTemplateType())
                .thenReturn(DemandTemplateTypeEnum.AIRCONDITIONING);
        Mockito.when(template.getDemandTemplateTag()).thenReturn(TEMPLATE_TAG);
        return template;
    }

    /**
     * Build one valid instance configuration for tag-query tests.
     *
     * @param instanceId instance ID
     * @param instanceType instance type
     * @param inBlacklisted whether the instance is blacklisted
     * @param shielded whether the instance is shielded
     * @return instance configuration
     * @since v7.260701.0
     */
    private @NonNull DemandInstanceConfigEntity buildInstanceConfigEntity(
            @NonNull String instanceId,
            @NonNull DemandInstanceTypeEnum instanceType,
            boolean inBlacklisted,
            boolean shielded) {
        DemandInstanceConfigIdentifier identifier = new DemandInstanceConfigIdentifier();
        identifier.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
        identifier.setInstance(new DemandInstanceValueObject(instanceType, instanceId));
        DemandInstanceConfigEntity entity = new DemandInstanceConfigEntity();
        entity.setIdentifier(identifier);
        entity.setConfig(new DemandInstanceConfigValueObject()
                .setInBlacklisted(inBlacklisted)
                .setShielded(shielded));
        return entity;
    }
}
