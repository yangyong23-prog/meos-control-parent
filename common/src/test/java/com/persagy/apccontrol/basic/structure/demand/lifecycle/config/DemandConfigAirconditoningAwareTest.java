package com.persagy.apccontrol.basic.structure.demand.lifecycle.config;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.DemandConfigAirconditioningCustomDTO;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.MidCalendarDayDateTagQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigAirconditioningCustomService;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.domain.identifier.DemandConfigIdentifier;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningEnvironmentQualityObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbQualityValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbSeasonValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningWorkConditionValueObject;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.airconditioning.DemandConfigAirConditioningAnalyzeStrategy;
import com.persagy.framework.common.context.ContextUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Compatibility tests for the original air-conditioning demand-config lifecycle.
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class DemandConfigAirconditoningAwareTest {

    /**
     * Project context used by calendar queries.
     *
     * @since v7.260701.0
     */
    private static final String CONTEXT_PROJECT_ID = "context-project-id";

    /**
     * Original project aggregate ID passed to the custom-config service.
     *
     * @since v7.260701.0
     */
    private static final String AGGREGATE_PROJECT_ID = "aggregate-project-id";

    /**
     * Demand template used by compatibility tests.
     *
     * @since v7.260701.0
     */
    private static final Long TEMPLATE_ID = 101L;

    /**
     * Historical date used to exercise materialized calendar tags.
     *
     * @since v7.260701.0
     */
    private static final LocalDate HISTORY_DATE = LocalDate.of(2000, 1, 15);

    /**
     * Original application context restored after this test class.
     *
     * @since v7.260701.0
     */
    private static @Nullable ApplicationContext originalApplicationContext;

    /**
     * Isolated application context supplying the strategy resolved by the original lifecycle.
     *
     * @since v7.260701.0
     */
    private static @Nullable StaticApplicationContext testApplicationContext;

    /**
     * Strategy resolved through the original template-type lookup.
     *
     * @since v7.260701.0
     */
    private static final DemandConfigAirConditioningAnalyzeStrategy CONFIG_ANALYZE_STRATEGY =
            Mockito.mock(DemandConfigAirConditioningAnalyzeStrategy.class);

    private MidCalendarClientService midCalendarClientService;
    private DemandConfigAirconditioningCustomService customService;
    private DemandConfigAirconditoningAware aware;

    /**
     * Register the strategy dependency used by the original lifecycle.
     *
     * @since v7.260701.0
     */
    @BeforeAll
    public static void setUpSpringUtil() {
        DemandConfigAirconditoningAwareTest.originalApplicationContext =
                SpringUtil.getApplicationContext();
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.getBeanFactory().registerSingleton(
                DemandConfigAirConditioningAnalyzeStrategy.BEAN_NAME,
                DemandConfigAirconditoningAwareTest.CONFIG_ANALYZE_STRATEGY);
        applicationContext.refresh();
        DemandConfigAirconditoningAwareTest.testApplicationContext = applicationContext;
        new SpringUtil().setApplicationContext(applicationContext);
    }

    /**
     * Restore the original static application context.
     *
     * @since v7.260701.0
     */
    @AfterAll
    public static void tearDownSpringUtil() {
        new SpringUtil().setApplicationContext(
                DemandConfigAirconditoningAwareTest.originalApplicationContext);
        if (DemandConfigAirconditoningAwareTest.testApplicationContext != null) {
            DemandConfigAirconditoningAwareTest.testApplicationContext.close();
        }
        DemandConfigAirconditoningAwareTest.testApplicationContext = null;
        DemandConfigAirconditoningAwareTest.originalApplicationContext = null;
    }

    /**
     * Initialize the original lifecycle with isolated collaborators.
     *
     * @since v7.260701.0
     */
    @BeforeEach
    public void setUp() {
        ContextUtil.setProjectId(DemandConfigAirconditoningAwareTest.CONTEXT_PROJECT_ID);
        Mockito.reset(DemandConfigAirconditoningAwareTest.CONFIG_ANALYZE_STRATEGY);
        Mockito.when(DemandConfigAirconditoningAwareTest.CONFIG_ANALYZE_STRATEGY.defaultConfig(
                        DemandInstanceTypeEnum.PROJECT))
                .thenReturn(DemandConfigAirconditioningValueObject::new);
        this.midCalendarClientService = Mockito.mock(MidCalendarClientService.class);
        this.customService = Mockito.mock(DemandConfigAirconditioningCustomService.class);
        this.aware = new DemandConfigAirconditoningAware(
                this.midCalendarClientService, this.customService);
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
     * Verify the original lifecycle keeps the aggregate project ID and nullable config date when
     * querying project custom data, then writes the result back as self configuration.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldPreserveOriginalProjectCustomConfigContract() {
        DemandConfigAirconditioningValueObject globalConfig =
                new DemandConfigAirconditioningValueObject();
        DemandConfigValueObject<DemandConfigAirconditioningValueObject> inheritedConfig =
                DemandConfigValueObject.ofDefault(globalConfig, null);
        DemandConfigAggregate<DemandConfigAirconditioningValueObject> aggregate = this.buildAggregate(
                DemandInstanceTypeEnum.PROJECT,
                DemandConfigAirconditoningAwareTest.AGGREGATE_PROJECT_ID,
                null,
                inheritedConfig);
        DemandConfigAirconditioningWorkConditionValueObject workCondition =
                new DemandConfigAirconditioningWorkConditionValueObject();
        DemandConfigAirconditioningCustomDTO customConfig =
                new DemandConfigAirconditioningCustomDTO();
        customConfig.setValueSelectType(BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_CARBON_EMISSIONS);
        customConfig.setWorkCondition(workCondition);
        Mockito.when(this.customService.getConfigData(
                        DemandConfigAirconditoningAwareTest.AGGREGATE_PROJECT_ID,
                        DemandConfigAirconditoningAwareTest.TEMPLATE_ID.toString(),
                        null))
                .thenReturn(customConfig);

        this.aware.afterAggregateInit(Collections.singletonList(aggregate));

        Assertions.assertSame(inheritedConfig, aggregate.getConfig());
        Assertions.assertSame(workCondition,
                aggregate.getFinalConfig().getGlobalConfig().getWorkCondition());
        Assertions.assertEquals(BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_CARBON_EMISSIONS,
                aggregate.getFinalConfig().getGlobalConfig().getValueSelect().getValueSelectType());
        Mockito.verify(this.customService).getConfigData(
                DemandConfigAirconditoningAwareTest.AGGREGATE_PROJECT_ID,
                DemandConfigAirconditoningAwareTest.TEMPLATE_ID.toString(),
                null);
        Mockito.verifyNoMoreInteractions(this.customService);
    }

    /**
     * Verify the original lifecycle stops the entire season rewrite when an earlier aggregate has
     * no complete temperature-quality configuration.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldPreserveOriginalIncompleteAggregateBatchAbort() {
        DemandConfigAirconditioningTdbSeasonValueObject customWinter = this.buildSeasonConfig(
                MidCalendarAirSeasonTypeEnum.AIR_WINTER, new BigDecimal("17.5"));
        DemandConfigAirconditioningTdbSeasonValueObject customSummer = this.buildSeasonConfig(
                MidCalendarAirSeasonTypeEnum.AIR_SUMMER, new BigDecimal("23.5"));
        DemandConfigAirconditioningValueObject completeConfig = this.buildAirconditioningConfig(
                Arrays.asList(customWinter, customSummer));
        DemandConfigAggregate<DemandConfigAirconditioningValueObject> incompleteAggregate =
                this.buildAggregate(
                        DemandInstanceTypeEnum.SPACE,
                        "incomplete-space",
                        DemandConfigAirconditoningAwareTest.HISTORY_DATE,
                        null);
        DemandConfigAggregate<DemandConfigAirconditioningValueObject> completeAggregate =
                this.buildAggregate(
                        DemandInstanceTypeEnum.SPACE,
                        "complete-space",
                        DemandConfigAirconditoningAwareTest.HISTORY_DATE,
                        DemandConfigValueObject.ofDefault(completeConfig, null));

        this.aware.afterAggregateInit(Arrays.asList(incompleteAggregate, completeAggregate));

        List<DemandConfigAirconditioningTdbSeasonValueObject> actualSeasons =
                completeConfig.getEnvironmentQuality().getTdbQuality().getSeasonQuality();
        Assertions.assertEquals(2, actualSeasons.size());
        Assertions.assertSame(customWinter, actualSeasons.get(0));
        Assertions.assertSame(customSummer, actualSeasons.get(1));
        Mockito.verify(this.midCalendarClientService).listCalendarDayDateTag(
                Mockito.eq(DemandConfigAirconditoningAwareTest.CONTEXT_PROJECT_ID),
                Mockito.any(MidCalendarDayDateTagQueryParam.class));
        Mockito.verifyNoInteractions(this.customService);
    }

    /**
     * Build one aggregate around an inherited effective configuration.
     *
     * @param instanceType aggregate instance type
     * @param instanceId aggregate instance ID
     * @param configDate aggregate config date, possibly null
     * @param inheritedConfig inherited effective configuration, possibly null
     * @return aggregate configured for the original lifecycle
     * @since v7.260701.0
     */
    private @NonNull DemandConfigAggregate<DemandConfigAirconditioningValueObject> buildAggregate(
            @NonNull DemandInstanceTypeEnum instanceType,
            @NonNull String instanceId,
            @Nullable LocalDate configDate,
            @Nullable DemandConfigValueObject<DemandConfigAirconditioningValueObject> inheritedConfig) {
        DemandConfigIdentifier identifier = new DemandConfigIdentifier();
        identifier.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
        identifier.setInheritConfigType(InheritConfigTypeEnum.OBJECT);
        identifier.setTemplateTag("default");
        identifier.setTemplateId(DemandConfigAirconditoningAwareTest.TEMPLATE_ID);
        identifier.setConfigDate(configDate);
        identifier.setInstance(new DemandInstanceValueObject(instanceType, instanceId));
        DemandConfigAggregate<DemandConfigAirconditioningValueObject> aggregate =
                new DemandConfigAggregate<>();
        aggregate.setIdentifier(identifier);
        aggregate.setParentConfig(inheritedConfig);
        return aggregate;
    }

    /**
     * Build an air-conditioning configuration containing the supplied seasonal values.
     *
     * @param seasonConfigs seasonal TDB configurations
     * @return air-conditioning global configuration
     * @since v7.260701.0
     */
    private @NonNull DemandConfigAirconditioningValueObject buildAirconditioningConfig(
            @NonNull List<DemandConfigAirconditioningTdbSeasonValueObject> seasonConfigs) {
        DemandConfigAirconditioningTdbQualityValueObject tdbQuality =
                new DemandConfigAirconditioningTdbQualityValueObject();
        tdbQuality.setSeasonQuality(seasonConfigs);
        DemandConfigAirconditioningEnvironmentQualityObject environmentQuality =
                new DemandConfigAirconditioningEnvironmentQualityObject();
        environmentQuality.setTdbQuality(tdbQuality);
        DemandConfigAirconditioningValueObject globalConfig =
                new DemandConfigAirconditioningValueObject();
        globalConfig.setEnvironmentQuality(environmentQuality);
        return globalConfig;
    }

    /**
     * Build one customized seasonal TDB configuration.
     *
     * @param airSeason air-conditioning season
     * @param lowerComfortLimit customized lower comfort limit
     * @return customized season configuration
     * @since v7.260701.0
     */
    private @NonNull DemandConfigAirconditioningTdbSeasonValueObject buildSeasonConfig(
            @NonNull MidCalendarAirSeasonTypeEnum airSeason,
            @NonNull BigDecimal lowerComfortLimit) {
        DemandConfigAirconditioningTdbSeasonValueObject seasonConfig =
                new DemandConfigAirconditioningTdbSeasonValueObject();
        seasonConfig.setAirSeason(airSeason);
        seasonConfig.setLowerComfortSeasonLimitValue(lowerComfortLimit);
        return seasonConfig;
    }
}
