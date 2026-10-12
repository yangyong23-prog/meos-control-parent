package com.persagy.apccontrol.basic.structure.demand.gateway;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfig;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandConfigConverter;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.instance.DemandInstanceGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigByInstanceBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.lifecycle.config.DemandConfigAirconditoningAware;
import com.persagy.apccontrol.basic.structure.demand.lifecycle.config.DemandConfigAirconditioningQueryEnricher;
import com.persagy.apccontrol.basic.structure.demand.lifecycle.config.DemandConfigLifeAware;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueObject;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigGroupRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigResultRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandInstanceRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandTemplateRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigByInstanceRepositoryBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.repository.scene.DemandSceneRepository;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.airconditioning.DemandConfigAirConditioningAnalyzeStrategy;
import com.persagy.framework.common.context.ContextUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Unit tests for demand-config Gateway query boundaries.
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class DemandConfigGatewayImplTest {

    /**
     * Current project used by aggregate initialization.
     *
     * @since v7.260701.0
     */
    private static final String PROJECT_ID = "project-id";

    /**
     * Demand-template ID used by query tests.
     *
     * @since v7.260701.0
     */
    private static final Long TEMPLATE_ID = 101L;

    /**
     * Context-level configuration converter.
     *
     * @since v7.260701.0
     */
    private static final DemandConfigConverter DEMAND_CONFIG_CONVERTER =
            Mockito.mock(DemandConfigConverter.class);

    /**
     * Context-level template repository.
     *
     * @since v7.260701.0
     */
    private static final DemandTemplateRepository DEMAND_TEMPLATE_REPOSITORY =
            Mockito.mock(DemandTemplateRepository.class);

    /**
     * Context-level scene repository.
     *
     * @since v7.260701.0
     */
    private static final DemandSceneRepository DEMAND_SCENE_REPOSITORY =
            Mockito.mock(DemandSceneRepository.class);

    /**
     * Context-level configuration repository.
     *
     * @since v7.260701.0
     */
    private static final DemandConfigRepository DEMAND_CONFIG_REPOSITORY =
            Mockito.mock(DemandConfigRepository.class);

    /**
     * Context-level configuration-group repository.
     *
     * @since v7.260701.0
     */
    private static final DemandConfigGroupRepository DEMAND_CONFIG_GROUP_REPOSITORY =
            Mockito.mock(DemandConfigGroupRepository.class);

    /**
     * Context-level demand-instance repository.
     *
     * @since v7.260701.0
     */
    private static final DemandInstanceRepository DEMAND_INSTANCE_REPOSITORY =
            Mockito.mock(DemandInstanceRepository.class);

    /**
     * Context-level instance hierarchy service.
     *
     * @since v7.260701.0
     */
    private static final InstanceService INSTANCE_SERVICE = Mockito.mock(InstanceService.class);

    /**
     * Context-level RWD client.
     *
     * @since v7.260701.0
     */
    private static final DtpRwdClientService DTP_RWD_CLIENT_SERVICE =
            Mockito.mock(DtpRwdClientService.class);

    /**
     * Air-conditioning configuration strategy resolved by template type.
     *
     * @since v7.260701.0
     */
    private static final DemandConfigAirConditioningAnalyzeStrategy CONFIG_ANALYZE_STRATEGY =
            Mockito.mock(DemandConfigAirConditioningAnalyzeStrategy.class);

    /**
     * Direct executor used by aggregate initialization.
     *
     * @since v7.260701.0
     */
    private static final Executor DIRECT_EXECUTOR = Runnable::run;

    /**
     * Original application context restored after this test class.
     *
     * @since v7.260701.0
     */
    private static @Nullable ApplicationContext originalApplicationContext;

    /**
     * Isolated application context supplying static context dependencies.
     *
     * @since v7.260701.0
     */
    private static @Nullable StaticApplicationContext testApplicationContext;

    /**
     * Original air-conditioning lifecycle observed by each Gateway boundary test.
     *
     * @since v7.260701.0
     */
    private DemandConfigAirconditoningAware airconditoningAware;

    /**
     * Other lifecycle hook that stable queries must continue invoking.
     *
     * @since v7.260701.0
     */
    private DemandConfigLifeAware<DemandConfigAirconditioningValueObject> stableAware;

    /**
     * Gateway under test.
     *
     * @since v7.260701.0
     */
    private DemandConfigGatewayImpl<DemandConfigAirconditioningValueObject> gateway;

    /**
     * Register isolated dependencies used by {@code DemandConfigAggregateInitByInstanceContext}.
     *
     * @since v7.260701.0
     */
    @BeforeAll
    public static void setUpSpringUtil() {
        DemandConfigGatewayImplTest.originalApplicationContext = SpringUtil.getApplicationContext();
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.getBeanFactory().registerSingleton(
                "demandConfigConverter", DemandConfigGatewayImplTest.DEMAND_CONFIG_CONVERTER);
        applicationContext.getBeanFactory().registerSingleton(
                "demandTemplateRepository", DemandConfigGatewayImplTest.DEMAND_TEMPLATE_REPOSITORY);
        applicationContext.getBeanFactory().registerSingleton(
                "demandSceneRepository", DemandConfigGatewayImplTest.DEMAND_SCENE_REPOSITORY);
        applicationContext.getBeanFactory().registerSingleton(
                "demandConfigRepository", DemandConfigGatewayImplTest.DEMAND_CONFIG_REPOSITORY);
        applicationContext.getBeanFactory().registerSingleton(
                "demandConfigGroupRepository", DemandConfigGatewayImplTest.DEMAND_CONFIG_GROUP_REPOSITORY);
        applicationContext.getBeanFactory().registerSingleton(
                "demandInstanceRepository", DemandConfigGatewayImplTest.DEMAND_INSTANCE_REPOSITORY);
        applicationContext.getBeanFactory().registerSingleton(
                "instanceService", DemandConfigGatewayImplTest.INSTANCE_SERVICE);
        applicationContext.getBeanFactory().registerSingleton(
                "dtpRwdClientService", DemandConfigGatewayImplTest.DTP_RWD_CLIENT_SERVICE);
        applicationContext.getBeanFactory().registerSingleton(
                "demandConfigExecutor", DemandConfigGatewayImplTest.DIRECT_EXECUTOR);
        applicationContext.getBeanFactory().registerSingleton(
                DemandConfigAirConditioningAnalyzeStrategy.BEAN_NAME,
                DemandConfigGatewayImplTest.CONFIG_ANALYZE_STRATEGY);
        applicationContext.refresh();
        DemandConfigGatewayImplTest.testApplicationContext = applicationContext;
        new SpringUtil().setApplicationContext(applicationContext);
    }

    /**
     * Restore the original static application context.
     *
     * @since v7.260701.0
     */
    @AfterAll
    public static void tearDownSpringUtil() {
        new SpringUtil().setApplicationContext(DemandConfigGatewayImplTest.originalApplicationContext);
        if (DemandConfigGatewayImplTest.testApplicationContext != null) {
            DemandConfigGatewayImplTest.testApplicationContext.close();
        }
        DemandConfigGatewayImplTest.testApplicationContext = null;
        DemandConfigGatewayImplTest.originalApplicationContext = null;
    }

    /**
     * Initialize deterministic Gateway collaborators and one project configuration row.
     *
     * @since v7.260701.0
     */
    @BeforeEach
    public void setUp() {
        ContextUtil.setProjectId(DemandConfigGatewayImplTest.PROJECT_ID);
        Mockito.reset(
                DemandConfigGatewayImplTest.DEMAND_CONFIG_CONVERTER,
                DemandConfigGatewayImplTest.DEMAND_TEMPLATE_REPOSITORY,
                DemandConfigGatewayImplTest.DEMAND_SCENE_REPOSITORY,
                DemandConfigGatewayImplTest.DEMAND_CONFIG_REPOSITORY,
                DemandConfigGatewayImplTest.DEMAND_CONFIG_GROUP_REPOSITORY,
                DemandConfigGatewayImplTest.DEMAND_INSTANCE_REPOSITORY,
                DemandConfigGatewayImplTest.INSTANCE_SERVICE,
                DemandConfigGatewayImplTest.DTP_RWD_CLIENT_SERVICE,
                DemandConfigGatewayImplTest.CONFIG_ANALYZE_STRATEGY);
        DemandTemplate template = this.buildTemplate();
        DemandConfig config = this.buildProjectConfig();
        Mockito.when(DemandConfigGatewayImplTest.DEMAND_CONFIG_CONVERTER.toBathQueryParam(
                        Mockito.any(DemandConfigByInstanceBatchQueryParam.class)))
                .thenAnswer(invocation -> new DemandConfigByInstanceRepositoryBatchQueryParam());
        Mockito.when(DemandConfigGatewayImplTest.DEMAND_TEMPLATE_REPOSITORY.getByTemplateIdAndDate(
                        Mockito.eq(true),
                        Mockito.eq(DemandConfigGatewayImplTest.TEMPLATE_ID),
                        Mockito.nullable(LocalDate.class)))
                .thenReturn(template);
        Mockito.when(DemandConfigGatewayImplTest.DEMAND_TEMPLATE_REPOSITORY.listByTemplateIds(Mockito.anySet()))
                .thenReturn(Collections.singletonList(template));
        Mockito.when(DemandConfigGatewayImplTest.DEMAND_CONFIG_REPOSITORY.listByInstances(
                        Mockito.any(DemandConfigByInstanceRepositoryBatchQueryParam.class)))
                .thenReturn(Collections.singletonList(config));
        Mockito.when(DemandConfigGatewayImplTest.DEMAND_INSTANCE_REPOSITORY.listConfig(Mockito.any()))
                .thenReturn(Collections.emptyList());
        Mockito.when(DemandConfigGatewayImplTest.INSTANCE_SERVICE.mapInstanceIdAndAllParentsByDirectQuery(
                        Mockito.anyMap(), Mockito.eq(false)))
                .thenReturn(Collections.emptyMap());
        Mockito.when(DemandConfigGatewayImplTest.DTP_RWD_CLIENT_SERVICE.mapObjectIdAndLocalName(
                        Mockito.anyCollection()))
                .thenReturn(Collections.emptyMap());
        Mockito.when(DemandConfigGatewayImplTest.CONFIG_ANALYZE_STRATEGY.inheritConfigType())
                .thenReturn(InheritConfigTypeEnum.OBJECT);
        Mockito.when(DemandConfigGatewayImplTest.CONFIG_ANALYZE_STRATEGY.defaultConfig(
                        DemandInstanceTypeEnum.PROJECT))
                .thenReturn(DemandConfigAirconditioningValueObject::new);
        this.airconditoningAware = Mockito.mock(DemandConfigAirconditoningAware.class);
        this.stableAware = Mockito.mock(DemandConfigLifeAware.class);
        this.gateway = new DemandConfigGatewayImpl<>(
                DemandConfigGatewayImplTest.DTP_RWD_CLIENT_SERVICE,
                DemandConfigGatewayImplTest.DEMAND_CONFIG_CONVERTER,
                DemandConfigGatewayImplTest.DEMAND_TEMPLATE_REPOSITORY,
                DemandConfigGatewayImplTest.DEMAND_SCENE_REPOSITORY,
                DemandConfigGatewayImplTest.DEMAND_INSTANCE_REPOSITORY,
                DemandConfigGatewayImplTest.DEMAND_CONFIG_REPOSITORY,
                DemandConfigGatewayImplTest.DEMAND_CONFIG_GROUP_REPOSITORY,
                DemandConfigGatewayImplTest.INSTANCE_SERVICE,
                Arrays.asList(this.airconditoningAware, this.stableAware),
                Mockito.mock(DemandConfigResultRepository.class),
                Mockito.mock(DemandInstanceGateway.class),
                Mockito.mock(DemandConfigAirconditioningQueryEnricher.class));
    }

    /**
     * Clear project context after each test.
     *
     * @since v7.260701.0
     */
    @AfterEach
    public void tearDown() {
        ContextUtil.remove();
    }

    /**
     * Verify the ordinary realtime query invokes the original air-conditioning lifecycle exactly once.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldInvokeOriginalLifecycleForOrdinaryConfigQuery() {
        DemandConfigByInstanceBatchQueryParam param = this.buildQueryParam();
        param.setQueryInheritConfig(false);
        param.setFillInstanceName(false);

        List<DemandConfigAggregate<DemandConfigAirconditioningValueObject>> results =
                this.gateway.listConfigsByInstances(param);

        Assertions.assertEquals(1, results.size());
        Mockito.verify(this.airconditoningAware).afterAggregateInit(
                Mockito.argThat(items -> items.size() == 1));
        Mockito.verify(this.stableAware).afterAggregateInit(
                Mockito.argThat(items -> items.size() == 1));
    }

    /**
     * Verify the stable query forces inheritance, disables names, and skips only the original
     * air-conditioning query lifecycle.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldForceStableQueryFlagsAndSkipOriginalAirconditioningLifecycle() {
        DemandConfigByInstanceBatchQueryParam param = this.buildQueryParam();
        param.setQueryInheritConfig(false);
        param.setFillInstanceName(true);

        List<DemandConfigAggregate<DemandConfigAirconditioningValueObject>> results =
                this.gateway.listStableConfigsByInstances(param);

        Assertions.assertEquals(1, results.size());
        Assertions.assertTrue(param.isQueryInheritConfig());
        Assertions.assertFalse(param.isFillInstanceName());
        Mockito.verify(this.airconditoningAware, Mockito.never())
                .afterAggregateInit(Mockito.anyList());
        Mockito.verify(this.stableAware).afterAggregateInit(
                Mockito.argThat(items -> items.size() == 1));
        Mockito.verify(DemandConfigGatewayImplTest.DTP_RWD_CLIENT_SERVICE, Mockito.never())
                .mapObjectIdAndLocalName(Mockito.anyCollection());
    }

    /**
     * Verify raw origin queries preserve the original lifecycle behavior.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldPreserveOriginalLifecycleForOriginConfigQuery() {
        DemandConfigByInstanceRepositoryBatchQueryParam param =
                new DemandConfigByInstanceRepositoryBatchQueryParam();

        List<DemandConfigAggregate<DemandConfigAirconditioningValueObject>> results =
                this.gateway.listOriginConfigs(param);

        Assertions.assertEquals(1, results.size());
        Mockito.verify(this.airconditoningAware).afterAggregateInit(
                Mockito.argThat(items -> items.size() == 1));
        Mockito.verify(this.stableAware).afterAggregateInit(
                Mockito.argThat(items -> items.size() == 1));
    }

    /**
     * Build the query scope shared by realtime and stable Gateway tests.
     *
     * @return project-scoped query parameter
     * @since v7.260701.0
     */
    private @NonNull DemandConfigByInstanceBatchQueryParam buildQueryParam() {
        DemandConfigByInstanceBatchQueryParam param = new DemandConfigByInstanceBatchQueryParam();
        param.setTemplateId(DemandConfigGatewayImplTest.TEMPLATE_ID);
        param.setInstanceTypeAndInstanceIds(Collections.singletonMap(
                DemandInstanceTypeEnum.PROJECT,
                Collections.singleton(DemandConfigGatewayImplTest.PROJECT_ID)));
        return param;
    }

    /**
     * Build the demand template resolved by aggregate initialization.
     *
     * @return air-conditioning template
     * @since v7.260701.0
     */
    private @NonNull DemandTemplate buildTemplate() {
        DemandTemplate template = new DemandTemplate();
        template.setId(DemandConfigGatewayImplTest.TEMPLATE_ID);
        template.setDemandType(DemandTypeEnum.AIRCONDITIONING);
        template.setDemandTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
        template.setDemandTemplateTag("default");
        return template;
    }

    /**
     * Build one raw project configuration row.
     *
     * @return project configuration with no custom JSON
     * @since v7.260701.0
     */
    private @NonNull DemandConfig buildProjectConfig() {
        DemandConfig config = new DemandConfig();
        config.setRelTemplateId(DemandConfigGatewayImplTest.TEMPLATE_ID);
        config.setDemandInstanceType(DemandInstanceTypeEnum.PROJECT);
        config.setDemandInstanceId(DemandConfigGatewayImplTest.PROJECT_ID);
        return config;
    }
}
