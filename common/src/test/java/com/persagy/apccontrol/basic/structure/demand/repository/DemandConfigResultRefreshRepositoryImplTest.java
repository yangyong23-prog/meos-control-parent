package com.persagy.apccontrol.basic.structure.demand.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigResult;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceConfig;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigByInstanceBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.domain.identifier.DemandConfigIdentifier;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandSceneEntity;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueSelectValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningWorkConditionValueObject;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigResultRepositoryQueryParam;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandInstanceConfigRepositoryQueryParam;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.framework.common.context.ContextUtil;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 需求配置结果模板类型刷新仓储测试。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
class DemandConfigResultRefreshRepositoryImplTest {

    private static final String SHIELDED_SPACE_ID = "space-0";

    private DemandConfigGateway<BaseDemandConfig> demandConfigGateway;
    private DemandTemplateRepository demandTemplateRepository;
    private DemandInstanceRepository demandInstanceRepository;
    private InstanceService instanceService;
    private DemandConfigResultRepository demandConfigResultRepository;
    private ExecutorService templateRefreshExecutor;

    /**
     * Initialize isolated repository collaborators and the bounded template-refresh executor.
     *
     * @since v7.260701.0
     */
    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        this.demandConfigGateway = Mockito.mock(DemandConfigGateway.class);
        this.demandTemplateRepository = Mockito.mock(DemandTemplateRepository.class);
        this.demandInstanceRepository = Mockito.mock(DemandInstanceRepository.class);
        this.instanceService = Mockito.mock(InstanceService.class);
        this.demandConfigResultRepository = Mockito.mock(DemandConfigResultRepository.class);
        this.templateRefreshExecutor = Executors.newFixedThreadPool(3);
        Mockito.when(this.demandConfigGateway.listStableConfigsByInstances(Mockito.any()))
                .thenReturn(Collections.emptyList());
        Mockito.when(this.demandConfigResultRepository.syncRefreshScope(
                        Mockito.any(DemandConfigResultRepositoryQueryParam.class), Mockito.anyCollection()))
                .thenReturn(Collections.emptyList());
    }

    /**
     * Stop the template-refresh executor after each test.
     *
     * @since v7.260701.0
     */
    @AfterEach
    void tearDown() {
        this.templateRefreshExecutor.shutdownNow();
    }

    /**
     * Verify shielded spaces are prefetched once and reused by every template refresh.
     *
     * @since v7.260701.0
     */
    @Test
    void shouldPrefetchShieldedSpacesOncePerTemplateTypeAndReuseForEveryTemplate() {
        List<Long> templateIds = Arrays.asList(101L, 102L, 103L, 104L);
        Mockito.when(this.demandTemplateRepository.listTemplateIdsByTemplateType(
                        DemandTemplateTypeEnum.AIRCONDITIONING))
                .thenReturn(templateIds);
        Mockito.when(this.demandInstanceRepository.listConfig(
                        Mockito.any(DemandInstanceConfigRepositoryQueryParam.class)))
                .thenAnswer(invocation -> {
                    DemandInstanceConfigRepositoryQueryParam queryParam = invocation.getArgument(0);
                    if (!queryParam.getInstanceIds().contains(
                            DemandConfigResultRefreshRepositoryImplTest.SHIELDED_SPACE_ID)) {
                        return Collections.emptyList();
                    }
                    DemandInstanceConfig config = new DemandInstanceConfig();
                    config.setDemandInstanceId(DemandConfigResultRefreshRepositoryImplTest.SHIELDED_SPACE_ID);
                    return Collections.singletonList(config);
                });
        TestDemandConfigResultRefreshRepository repository =
                this.newRepository(this.demandScope(501));

        repository.refreshByTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);

        ArgumentCaptor<DemandInstanceConfigRepositoryQueryParam> shieldQueryCaptor =
                ArgumentCaptor.forClass(DemandInstanceConfigRepositoryQueryParam.class);
        Mockito.verify(this.demandInstanceRepository, Mockito.times(2))
                .listConfig(shieldQueryCaptor.capture());
        Assertions.assertEquals(501, shieldQueryCaptor.getAllValues().stream()
                .map(DemandInstanceConfigRepositoryQueryParam::getInstanceIds)
                .mapToInt(Set::size)
                .sum());
        Assertions.assertTrue(shieldQueryCaptor.getAllValues().stream()
                .allMatch(queryParam -> queryParam.getInstanceIds().size() <= 500));
        Assertions.assertTrue(shieldQueryCaptor.getAllValues().stream()
                .allMatch(queryParam -> queryParam.getTemplateType() == DemandTemplateTypeEnum.AIRCONDITIONING
                        && queryParam.getInstanceType() == DemandInstanceTypeEnum.SPACE));

        List<Set<String>> prefetchedShieldedSpaceIds = repository.listPrefetchedShieldedSpaceIds();
        Assertions.assertEquals(templateIds.size(), prefetchedShieldedSpaceIds.size());
        Set<String> firstPrefetchedSet = prefetchedShieldedSpaceIds.get(0);
        Assertions.assertTrue(firstPrefetchedSet.contains(
                DemandConfigResultRefreshRepositoryImplTest.SHIELDED_SPACE_ID));
        prefetchedShieldedSpaceIds.forEach(prefetchedSet ->
                Assertions.assertSame(firstPrefetchedSet, prefetchedSet));
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> firstPrefetchedSet.add("unexpected-space"));

        Mockito.verify(this.demandConfigGateway, Mockito.times(templateIds.size()))
                .listStableConfigsByInstances(Mockito.any());
        Mockito.verify(this.demandConfigGateway, Mockito.never())
                .listConfigsByInstances(Mockito.any());
        ArgumentCaptor<DemandConfigResultRepositoryQueryParam> refreshScopeCaptor =
                ArgumentCaptor.forClass(DemandConfigResultRepositoryQueryParam.class);
        Mockito.verify(this.demandConfigResultRepository, Mockito.times(templateIds.size()))
                .syncRefreshScope(refreshScopeCaptor.capture(), Mockito.anyCollection());
        Assertions.assertEquals(new LinkedHashSet<>(templateIds), refreshScopeCaptor.getAllValues().stream()
                .map(DemandConfigResultRepositoryQueryParam::getRelTemplateId)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        Assertions.assertTrue(refreshScopeCaptor.getAllValues().stream()
                .allMatch(DemandConfigResultRepositoryQueryParam::getRelSceneIdIsNull));
    }

    /**
     * Verify an immutable non-null empty shielded-space set is reused when no space is shielded.
     *
     * @since v7.260701.0
     */
    @Test
    void shouldReuseNonNullEmptyShieldedSpaceSetWhenNoSpaceIsShielded() {
        List<Long> templateIds = Arrays.asList(201L, 202L);
        Mockito.when(this.demandTemplateRepository.listTemplateIdsByTemplateType(
                        DemandTemplateTypeEnum.AIRCONDITIONING))
                .thenReturn(templateIds);
        Mockito.when(this.demandInstanceRepository.listConfig(
                        Mockito.any(DemandInstanceConfigRepositoryQueryParam.class)))
                .thenReturn(Collections.emptyList());
        TestDemandConfigResultRefreshRepository repository =
                this.newRepository(this.demandScope(1));

        repository.refreshByTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);

        Mockito.verify(this.demandInstanceRepository, Mockito.times(1))
                .listConfig(Mockito.any(DemandInstanceConfigRepositoryQueryParam.class));
        List<Set<String>> prefetchedShieldedSpaceIds = repository.listPrefetchedShieldedSpaceIds();
        Assertions.assertEquals(templateIds.size(), prefetchedShieldedSpaceIds.size());
        Set<String> firstPrefetchedSet = prefetchedShieldedSpaceIds.get(0);
        Assertions.assertNotNull(firstPrefetchedSet);
        Assertions.assertTrue(firstPrefetchedSet.isEmpty());
        prefetchedShieldedSpaceIds.forEach(prefetchedSet ->
                Assertions.assertSame(firstPrefetchedSet, prefetchedSet));
        Mockito.verify(this.demandConfigResultRepository, Mockito.times(templateIds.size()))
                .syncRefreshScope(Mockito.any(DemandConfigResultRepositoryQueryParam.class), Mockito.anyCollection());
    }

    /**
     * Verify template-type orchestration stays outside a transaction while each scope write is transactional.
     *
     * @throws NoSuchMethodException when the inspected method signature changes
     * @since v7.260701.0
     */
    @Test
    void shouldKeepTemplateTypeOrchestrationOutsideTransactionAndEachTemplateWriteTransactional()
            throws NoSuchMethodException {
        Method refreshByTemplateType = DemandConfigResultRefreshRepositoryImpl.class.getMethod(
                "refreshByTemplateType", DemandTemplateTypeEnum.class);
        Assertions.assertNull(refreshByTemplateType.getAnnotation(Transactional.class));

        Method syncRefreshScope = DemandConfigResultRepositoryImpl.class.getMethod(
                "syncRefreshScope", DemandConfigResultRepositoryQueryParam.class, Collection.class);
        Transactional transactional = syncRefreshScope.getAnnotation(Transactional.class);
        Assertions.assertNotNull(transactional);
        Assertions.assertTrue(Arrays.asList(transactional.rollbackFor()).contains(Exception.class));
    }

    /**
     * Verify the public incremental refresh entry keeps the repository-level support guard.
     *
     * @since v7.260701.0
     */
    @Test
    void shouldIgnoreUnsupportedTemplateBeforeExpandingInstances() {
        DemandConfigResultRefreshRepositoryImpl<BaseDemandConfig> repository =
                new DemandConfigResultRefreshRepositoryImpl<BaseDemandConfig>(
                        this.demandConfigGateway,
                        this.demandTemplateRepository,
                        this.demandInstanceRepository,
                        this.instanceService,
                        this.demandConfigResultRepository) {
                    @Override
                    public boolean supportsTemplate(@NonNull Long templateId) {
                        return false;
                    }
                };

        repository.refreshByInstanceAndChildren(
                101L,
                null,
                DemandInstanceTypeEnum.BUILDING,
                Collections.singleton("building-1"));

        Mockito.verifyNoInteractions(this.instanceService);
        Mockito.verifyNoInteractions(this.demandConfigGateway);
        Mockito.verifyNoInteractions(this.demandConfigResultRepository);
    }

    /**
     * Verify physically expanded non-demand spaces are filtered out before stable calculation.
     *
     * @since v7.260801.0
     */
    @Test
    void shouldFilterOutNonDemandSpacesWhenRefreshingInstanceAndChildren() {
        this.stubProjectPhysicalExpansion();
        TestDemandConfigResultRefreshRepository repository =
                this.newRepository(this.demandScope(0));

        repository.refreshByInstanceAndChildren(
                101L, null, DemandInstanceTypeEnum.PROJECT, Collections.singleton("project-1"));

        Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds =
                this.captureStableQueryParam().getInstanceTypeAndInstanceIds();
        Assertions.assertEquals(
                Collections.singleton("demand-space-1"),
                instanceTypeAndInstanceIds.get(DemandInstanceTypeEnum.SPACE));
        Assertions.assertEquals(
                Collections.singleton("floor-1"),
                instanceTypeAndInstanceIds.get(DemandInstanceTypeEnum.FLOOR));
        Assertions.assertEquals(
                Collections.singleton("building-1"),
                instanceTypeAndInstanceIds.get(DemandInstanceTypeEnum.BUILDING));
        Assertions.assertEquals(
                Collections.singleton("project-1"),
                instanceTypeAndInstanceIds.get(DemandInstanceTypeEnum.PROJECT));
    }

    /**
     * Verify only the project instance survives when the template type has no demand spaces.
     *
     * @since v7.260801.0
     */
    @Test
    void shouldKeepOnlyProjectInstanceWhenNoDemandSpaces() {
        this.stubProjectPhysicalExpansion();
        DemandConfigResultRefreshRepositoryImpl<BaseDemandConfig> repository =
                new DemandConfigResultRefreshRepositoryImpl<BaseDemandConfig>(
                        this.demandConfigGateway,
                        this.demandTemplateRepository,
                        this.demandInstanceRepository,
                        this.instanceService,
                        this.demandConfigResultRepository) {
                    @Override
                    @NonNull DemandTemplate queryTemplate(@NonNull Long templateId) {
                        DemandTemplate template = new DemandTemplate();
                        template.setId(templateId);
                        template.setDemandTemplateType(DemandTemplateTypeEnum.PARKING_VENTILATION);
                        return template;
                    }

                    @Override
                    @NonNull List<DtpRwdSpaceDTO> listDemandSpaces(
                            @NonNull DemandTemplateTypeEnum templateType) {
                        return Collections.emptyList();
                    }
                };

        repository.refreshByInstanceAndChildren(
                101L, null, DemandInstanceTypeEnum.PROJECT, Collections.singleton("project-1"));

        Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds =
                this.captureStableQueryParam().getInstanceTypeAndInstanceIds();
        Assertions.assertEquals(
                Collections.singleton("project-1"),
                instanceTypeAndInstanceIds.get(DemandInstanceTypeEnum.PROJECT));
        Assertions.assertFalse(instanceTypeAndInstanceIds.containsKey(DemandInstanceTypeEnum.SPACE));
        Assertions.assertFalse(instanceTypeAndInstanceIds.containsKey(DemandInstanceTypeEnum.FLOOR));
        Assertions.assertFalse(instanceTypeAndInstanceIds.containsKey(DemandInstanceTypeEnum.BUILDING));
    }

    /**
     * Verify a demand space keeps refreshing when the source instance is a single space.
     *
     * @since v7.260801.0
     */
    @Test
    void shouldKeepDemandSpaceWhenRefreshingSingleSpace() {
        TestDemandConfigResultRefreshRepository repository =
                this.newRepository(this.demandScope(0));

        repository.refreshByInstanceAndChildren(
                101L, null, DemandInstanceTypeEnum.SPACE, Collections.singleton("demand-space-1"));

        Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds =
                this.captureStableQueryParam().getInstanceTypeAndInstanceIds();
        Assertions.assertEquals(
                Collections.singleton("demand-space-1"),
                instanceTypeAndInstanceIds.get(DemandInstanceTypeEnum.SPACE));
    }

    /**
     * Verify a non-demand space refresh is skipped without touching the gateway or result repository.
     *
     * @since v7.260801.0
     */
    @Test
    void shouldSkipRefreshWhenSingleSpaceIsNotDemandSpace() {
        TestDemandConfigResultRefreshRepository repository =
                this.newRepository(this.demandScope(0));

        repository.refreshByInstanceAndChildren(
                101L, null, DemandInstanceTypeEnum.SPACE, Collections.singleton("non-demand-space-2"));

        Mockito.verify(this.demandConfigGateway, Mockito.never())
                .listStableConfigsByInstances(Mockito.any());
        Mockito.verifyNoInteractions(this.demandConfigResultRepository);
    }

    /**
     * Capture the stable-config query parameter after an instance-and-children refresh.
     *
     * @return stable-config query parameter
     * @since v7.260801.0
     */
    private @NonNull DemandConfigByInstanceBatchQueryParam captureStableQueryParam() {
        ArgumentCaptor<DemandConfigByInstanceBatchQueryParam> queryCaptor =
                ArgumentCaptor.forClass(DemandConfigByInstanceBatchQueryParam.class);
        Mockito.verify(this.demandConfigGateway).listStableConfigsByInstances(queryCaptor.capture());
        return queryCaptor.getValue();
    }

    /**
     * Stub physical hierarchy expansion for a project-scoped refresh.
     *
     * @since v7.260801.0
     */
    private void stubProjectPhysicalExpansion() {
        Mockito.when(this.instanceService.listBuildingIdsByInstance(
                        DemandInstanceTypeEnum.PROJECT, Collections.singleton("project-1")))
                .thenReturn(Collections.singleton("building-1"));
        Mockito.when(this.instanceService.listFloorIdsByInstance(
                        DemandInstanceTypeEnum.PROJECT, Collections.singleton("project-1")))
                .thenReturn(Collections.singleton("floor-1"));
        Mockito.when(this.instanceService.listSpaceIdsByInstance(
                        DemandInstanceTypeEnum.PROJECT, Collections.singleton("project-1")))
                .thenReturn(new LinkedHashSet<>(Arrays.asList("demand-space-1", "non-demand-space-2")));
    }

    /**
     * Verify security templates are enabled through the shared demand-type support switch while lighting stays disabled.
     *
     * @since v7.260802.0
     */
    @Test
    void shouldSupportSecurityTemplateTypesThroughSharedDemandTypeSwitch() {
        DemandConfigResultRefreshRepositoryImpl<BaseDemandConfig> repository =
                this.newRepository(Collections.emptyMap());

        Assertions.assertTrue(repository.listSupportedTemplateTypes().contains(DemandTemplateTypeEnum.ARMING));
        Assertions.assertTrue(repository.listSupportedTemplateTypes().contains(DemandTemplateTypeEnum.PASSING));
        Assertions.assertTrue(repository.supportsTemplateType(DemandTemplateTypeEnum.ARMING));
        Assertions.assertTrue(repository.supportsTemplateType(DemandTemplateTypeEnum.PASSING));
        Assertions.assertFalse(repository.supportsTemplateType(DemandTemplateTypeEnum.PUBLIC_AREA_LIGHTING));
    }

    /**
     * Verify aggregate customization, prefetched shielding, and real scene IDs survive result conversion.
     *
     * @since v7.260701.0
     */
    @Test
    @SuppressWarnings("unchecked")
    void shouldPreserveBusinessValuesWhenConvertingAggregateToResult() throws ReflectiveOperationException {
        DemandInstanceValueObject instance = new DemandInstanceValueObject(
                DemandInstanceTypeEnum.SPACE, SHIELDED_SPACE_ID);
        DemandSceneEntity scene = new DemandSceneEntity();
        scene.setId(301L);
        DemandConfigIdentifier identifier = new DemandConfigIdentifier();
        identifier.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
        identifier.setTemplateId(101L);
        identifier.setScene(scene);
        identifier.setInstance(instance);

        DemandConfigAirconditioningValueObject globalConfig = new DemandConfigAirconditioningValueObject();
        globalConfig.setValueSelect(new DemandConfigAirconditioningValueSelectValueObject());
        globalConfig.setWorkCondition(new DemandConfigAirconditioningWorkConditionValueObject());
        DemandConfigValueObject<BaseDemandConfig> finalConfig = new DemandConfigValueObject<>();
        finalConfig.setGlobalConfig(globalConfig);
        DemandConfigAggregate<BaseDemandConfig> aggregate = Mockito.mock(DemandConfigAggregate.class);
        Mockito.when(aggregate.getIdentifier()).thenReturn(identifier);
        Mockito.when(aggregate.getFinalConfig()).thenReturn(finalConfig);
        Mockito.when(aggregate.getInheritInstanceAndModes()).thenReturn(new LinkedHashMap<>());
        Mockito.when(aggregate.isCustomizedConfig()).thenReturn(true);
        Map<DemandTemplateTypeEnum, Set<String>> shieldedSpaceIds =
                new EnumMap<>(DemandTemplateTypeEnum.class);
        shieldedSpaceIds.put(
                DemandTemplateTypeEnum.AIRCONDITIONING,
                Collections.singleton(SHIELDED_SPACE_ID));

        Field objectMapperField = ObjectMapperUtils.class.getDeclaredField("objectMapper");
        objectMapperField.setAccessible(true);
        Object originalObjectMapper = objectMapperField.get(null);
        objectMapperField.set(null, new ObjectMapper());
        ContextUtil.setProjectId("project-1");
        try {
            DemandConfigResult result = this.invokeToUpsertParam(
                    this.newRepository(Collections.emptyMap()), aggregate, shieldedSpaceIds);

            Assertions.assertEquals(Boolean.TRUE, result.getIsCustomizedConfig());
            Assertions.assertEquals(Boolean.TRUE, result.getIsShielded());
            Assertions.assertEquals(scene.getId(), result.getRelSceneId());
            JsonNode globalConfigJson = result.getFinalConfigJson()
                    .get(DemandConfigValueObject.Fields.globalConfig);
            Assertions.assertNotNull(globalConfigJson);
            Assertions.assertFalse(globalConfigJson.has(
                    DemandConfigAirconditioningValueObject.Fields.valueSelect));
            Assertions.assertFalse(globalConfigJson.has(
                    DemandConfigAirconditioningValueObject.Fields.workCondition));
            Assertions.assertNotNull(globalConfig.getValueSelect());
            Assertions.assertNotNull(globalConfig.getWorkCondition());
        } finally {
            ContextUtil.remove();
            objectMapperField.set(null, originalObjectMapper);
        }
    }

    /**
     * Create a refresh repository with isolated collaborators.
     *
     * @param demandScope prefetched demand scope
     * @return refresh repository
     * @since v7.260701.0
     */
    private @NonNull TestDemandConfigResultRefreshRepository newRepository(
            @NonNull Map<DemandInstanceTypeEnum, Set<String>> demandScope) {
        return new TestDemandConfigResultRefreshRepository(
                this.demandConfigGateway,
                this.demandTemplateRepository,
                this.demandInstanceRepository,
                this.instanceService,
                this.demandConfigResultRepository,
                this.templateRefreshExecutor,
                demandScope);
    }

    /**
     * Build a deterministic demand scope for template-type refresh tests.
     *
     * @param spaceCount space count
     * @return demand scope by instance type
     * @since v7.260701.0
     */
    private @NonNull Map<DemandInstanceTypeEnum, Set<String>> demandScope(int spaceCount) {
        Map<DemandInstanceTypeEnum, Set<String>> result = new EnumMap<>(DemandInstanceTypeEnum.class);
        result.put(DemandInstanceTypeEnum.PROJECT, Collections.singleton("project-1"));
        result.put(DemandInstanceTypeEnum.SPACE, IntStream.range(0, spaceCount)
                .mapToObj(index -> "space-" + index)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        return result;
    }

    /**
     * Invoke the private aggregate-to-result conversion without widening production visibility.
     *
     * @param repository refresh repository
     * @param aggregate demand-config aggregate
     * @param shieldedSpaceIds prefetched shielded space IDs by template type
     * @return converted result entity
     * @throws ReflectiveOperationException when the conversion method cannot be invoked
     * @since v7.260701.0
     */
    private @NonNull DemandConfigResult invokeToUpsertParam(
            @NonNull DemandConfigResultRefreshRepositoryImpl<BaseDemandConfig> repository,
            @NonNull DemandConfigAggregate<BaseDemandConfig> aggregate,
            @NonNull Map<DemandTemplateTypeEnum, Set<String>> shieldedSpaceIds)
            throws ReflectiveOperationException {
        Method method = DemandConfigResultRefreshRepositoryImpl.class.getDeclaredMethod(
                "toUpsertParam", DemandConfigAggregate.class, Map.class);
        method.setAccessible(true);
        return (DemandConfigResult) method.invoke(repository, aggregate, shieldedSpaceIds);
    }

    /**
     * Refresh repository test double that records prefetched shielded-space sets.
     *
     * @author JiYinchuan
     * @since v7.260701.0
     */
    private static final class TestDemandConfigResultRefreshRepository
            extends DemandConfigResultRefreshRepositoryImpl<BaseDemandConfig> {

        private final @NonNull Executor templateRefreshExecutor;
        private final @NonNull Map<DemandInstanceTypeEnum, Set<String>> demandScope;
        private final @NonNull Queue<Set<String>> prefetchedShieldedSpaceIds = new ConcurrentLinkedQueue<>();

        /**
         * Create a repository test double with isolated collaborators.
         *
         * @param demandConfigGateway realtime demand-config gateway
         * @param demandTemplateRepository demand-template repository
         * @param demandInstanceRepository demand-instance repository
         * @param instanceService instance service
         * @param demandConfigResultRepository result repository
         * @param templateRefreshExecutor template-refresh executor
         * @param demandScope prefetched demand scope
         * @since v7.260701.0
         */
        private TestDemandConfigResultRefreshRepository(
                @NonNull DemandConfigGateway<BaseDemandConfig> demandConfigGateway,
                @NonNull DemandTemplateRepository demandTemplateRepository,
                @NonNull DemandInstanceRepository demandInstanceRepository,
                @NonNull InstanceService instanceService,
                @NonNull DemandConfigResultRepository demandConfigResultRepository,
                @NonNull Executor templateRefreshExecutor,
                @NonNull Map<DemandInstanceTypeEnum, Set<String>> demandScope) {
            super(demandConfigGateway, demandTemplateRepository, demandInstanceRepository,
                    instanceService, demandConfigResultRepository);
            this.templateRefreshExecutor = templateRefreshExecutor;
            this.demandScope = demandScope;
        }

        @Override
        @NonNull Executor templateRefreshExecutor() {
            return this.templateRefreshExecutor;
        }

        @Override
        @NonNull DemandTemplate queryTemplate(@NonNull Long templateId) {
            DemandTemplate template = new DemandTemplate();
            template.setId(templateId);
            template.setDemandTemplateType(DemandTemplateTypeEnum.PARKING_VENTILATION);
            return template;
        }

        @Override
        @NonNull List<DtpRwdSpaceDTO> listDemandSpaces(
                @NonNull DemandTemplateTypeEnum templateType) {
            DtpRwdSpaceDTO demandSpace = new DtpRwdSpaceDTO();
            demandSpace.setId("demand-space-1");
            demandSpace.setFloorId("floor-1");
            demandSpace.setBuildingId("building-1");
            return Collections.singletonList(demandSpace);
        }

        @Override
        @NonNull Map<DemandInstanceTypeEnum, Set<String>> toDemandScopeInstanceTypeAndInstanceIds(
                @NonNull DemandTemplateTypeEnum templateType) {
            return this.demandScope;
        }

        @Override
        void refreshTemplate(
                @NonNull DemandTemplateTypeEnum templateType,
                @NonNull Long templateId,
                @NonNull Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds,
                @NonNull Set<String> shieldedSpaceIds) {
            this.prefetchedShieldedSpaceIds.add(shieldedSpaceIds);
            super.refreshTemplate(templateType, templateId, instanceTypeAndInstanceIds, shieldedSpaceIds);
        }

        /**
         * Return recorded shielded-space sets in refresh invocation order.
         *
         * @return recorded prefetched shielded-space sets
         * @since v7.260701.0
         */
        private @NonNull List<Set<String>> listPrefetchedShieldedSpaceIds() {
            return new ArrayList<>(this.prefetchedShieldedSpaceIds);
        }
    }
}
