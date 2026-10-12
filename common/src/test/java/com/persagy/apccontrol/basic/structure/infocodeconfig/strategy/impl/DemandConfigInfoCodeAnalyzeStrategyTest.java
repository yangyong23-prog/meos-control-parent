package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.param.demand.DemandConfigResultBatchQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.DemandConfigResultVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateDateSettingVO;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.service.demand.DemandCalendarQueryService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningCO2QualityValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningEnvironmentQualityObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningPressureDiffQualityValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningRhQualityValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningRhSeasonValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbQualityValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbSeasonValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueObject;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandTemplateRepository;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Demand-config InfoCode result-query regression tests.
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class DemandConfigInfoCodeAnalyzeStrategyTest {

    private static final String PROJECT_ID = "project-id";
    private static final Long TEMPLATE_ID = 101L;
    private static final String CONFIGURED_SPACE_ID = "space-configured";
    private static final String EMPTY_CONFIG_SPACE_ID = "space-empty-config";

    private static final DemandCalendarQueryService DEMAND_CALENDAR_QUERY_SERVICE =
            Mockito.mock(DemandCalendarQueryService.class);
    private static final DemandTemplateRepository DEMAND_TEMPLATE_REPOSITORY =
            Mockito.mock(DemandTemplateRepository.class);

    private static @Nullable ApplicationContext originalApplicationContext;
    private static @Nullable StaticApplicationContext testApplicationContext;

    private MidCalendarClientService midCalendarClientService;
    private DemandConfigGateway<DemandConfigAirconditioningValueObject> demandConfigGateway;
    private DemandConfigInfoCodeAnalyzeStrategy strategy;

    /**
     * Save the existing context and register template-query collaborators used by {@link SpringUtil}.
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
        applicationContext.refresh();
        testApplicationContext = applicationContext;
        new SpringUtil().setApplicationContext(applicationContext);
    }

    /**
     * Restore the original {@link SpringUtil} context and close the isolated test context.
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
     * Initialize isolated strategy collaborators and current-project context.
     *
     * @since v7.260701.0
     */
    @BeforeEach
    public void setUp() {
        Mockito.reset(DEMAND_CALENDAR_QUERY_SERVICE, DEMAND_TEMPLATE_REPOSITORY);
        ContextUtil.setProjectId(PROJECT_ID);
        this.midCalendarClientService = Mockito.mock(MidCalendarClientService.class);
        this.demandConfigGateway = Mockito.mock(DemandConfigGateway.class);
        this.strategy = new DemandConfigInfoCodeAnalyzeStrategy(
                this.midCalendarClientService, this.demandConfigGateway);
        this.stubCurrentTemplate();
        Mockito.when(this.midCalendarClientService.getAirSeasonType(
                        Mockito.eq(PROJECT_ID), Mockito.any(LocalDate.class)))
                .thenReturn(MidCalendarAirSeasonTypeEnum.AIR_SUMMER);
    }

    /**
     * Clear the thread-local project context after each test.
     *
     * @since v7.260701.0
     */
    @AfterEach
    public void tearDown() {
        ContextUtil.remove();
    }

    /**
     * Verify the result query is issued once with the required business scope and does not fall back on a miss.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldQueryConfigResultsOnceAndTreatMissAsNoConfig() {
        Set<String> spaceIds = Collections.singleton(CONFIGURED_SPACE_ID);
        InfoCodeAggregate aggregate = this.mockAggregate(
                CONFIGURED_SPACE_ID, DtpRwdSpaceDTO.Fields.tdb);
        Mockito.when(this.demandConfigGateway.listConfigResultsByInstances(
                        Mockito.any(DemandConfigResultBatchQueryParam.class)))
                .thenReturn(Collections.emptyList());

        this.strategy.fillInfoCodeDataModel(Collections.singletonList(aggregate), null);

        ArgumentCaptor<DemandConfigResultBatchQueryParam> paramCaptor =
                ArgumentCaptor.forClass(DemandConfigResultBatchQueryParam.class);
        Mockito.verify(this.demandConfigGateway, Mockito.times(1))
                .listConfigResultsByInstances(paramCaptor.capture());
        DemandConfigResultBatchQueryParam actualParam = paramCaptor.getValue();
        Assertions.assertEquals(TEMPLATE_ID, actualParam.getTemplateId());
        Assertions.assertEquals(DateUtils.now().toLocalDate(), actualParam.getDate());
        Assertions.assertNull(actualParam.getSceneId());
        Assertions.assertEquals(DemandInstanceTypeEnum.SPACE, actualParam.getInstanceType());
        Assertions.assertEquals(spaceIds, actualParam.getInstanceIds());
        Assertions.assertFalse(actualParam.isFillInstanceName());
        Mockito.verifyNoMoreInteractions(this.demandConfigGateway);
        Mockito.verify(aggregate, Mockito.never())
                .setInfoCodeAndDataModel(Mockito.any(InfoCodeDataModel.class));
        Assertions.assertTrue(Arrays.stream(DemandConfigInfoCodeAnalyzeStrategy.class.getDeclaredFields())
                .anyMatch(field -> DemandConfigGateway.class.isAssignableFrom(field.getType())));
    }

    /**
     * Verify precomputed final configurations retain seasonal TDB/RH, CO2, and pressure-difference mapping.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldMapCurrentSeasonConfigAndSkipEmptyResults() {
        InfoCodeAggregate tdbAggregate = this.mockAggregate(
                CONFIGURED_SPACE_ID, DtpRwdSpaceDTO.Fields.tdb);
        InfoCodeAggregate rhAggregate = this.mockAggregate(
                CONFIGURED_SPACE_ID, DtpRwdSpaceDTO.Fields.rh);
        InfoCodeAggregate co2Aggregate = this.mockAggregate(
                CONFIGURED_SPACE_ID, DtpRwdSpaceDTO.Fields.co2);
        InfoCodeAggregate pressureAggregate = this.mockAggregate(
                CONFIGURED_SPACE_ID, InfoCodeEnum.RELATIVE_PRESSURE_DIFF.getCode());
        InfoCodeAggregate emptyConfigAggregate = this.mockAggregate(
                EMPTY_CONFIG_SPACE_ID, DtpRwdSpaceDTO.Fields.tdb);
        List<InfoCodeAggregate> aggregates = Arrays.asList(
                tdbAggregate,
                rhAggregate,
                co2Aggregate,
                pressureAggregate,
                emptyConfigAggregate);
        Mockito.when(this.demandConfigGateway.listConfigResultsByInstances(
                        Mockito.any(DemandConfigResultBatchQueryParam.class)))
                .thenReturn(Arrays.asList(
                        this.buildConfiguredResult(),
                        this.buildEmptyConfigResult()));

        this.strategy.fillInfoCodeDataModel(aggregates, null);

        this.assertMappedDecimal(tdbAggregate, "lowerComfortSeasonLimitValue", "24");
        this.assertMappedDecimal(rhAggregate, "lowerComfortSeasonLimitValue", "40");
        this.assertMappedDecimal(co2Aggregate, "carbonDioxideLimitValue", "800");
        this.assertMappedDecimal(pressureAggregate, "lowerLimitValue", "5");
        Mockito.verify(emptyConfigAggregate, Mockito.never())
                .setInfoCodeAndDataModel(Mockito.any(InfoCodeDataModel.class));
        Mockito.verify(this.demandConfigGateway, Mockito.times(1))
                .listConfigResultsByInstances(Mockito.any(DemandConfigResultBatchQueryParam.class));
        Mockito.verifyNoMoreInteractions(this.demandConfigGateway);
    }

    /**
     * Stub the current air-conditioning template lookup without external services.
     *
     * @since v7.260701.0
     */
    private void stubCurrentTemplate() {
        DemandTemplateDateSettingVO dateSetting =
                Mockito.mock(DemandTemplateDateSettingVO.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(dateSetting.getRelTemplate().getTemplateId()).thenReturn(TEMPLATE_ID);
        Mockito.when(DEMAND_CALENDAR_QUERY_SERVICE.getDateSetting(
                        Mockito.eq(DemandTemplateTypeEnum.AIRCONDITIONING), Mockito.any(LocalDate.class)))
                .thenReturn(dateSetting);

        DemandTemplate demandTemplate = Mockito.mock(DemandTemplate.class);
        Mockito.when(demandTemplate.getId()).thenReturn(TEMPLATE_ID);
        Mockito.when(demandTemplate.getDemandTemplateType())
                .thenReturn(DemandTemplateTypeEnum.AIRCONDITIONING);
        Mockito.when(DEMAND_TEMPLATE_REPOSITORY.getByTemplateIdAndDate(
                        Mockito.eq(false), Mockito.eq(TEMPLATE_ID), Mockito.any(LocalDate.class)))
                .thenReturn(demandTemplate);
    }

    /**
     * Build an InfoCode aggregate mock for one space and code.
     *
     * @param instanceId space ID
     * @param infoCode InfoCode
     * @return aggregate mock
     * @since v7.260701.0
     */
    private @NonNull InfoCodeAggregate mockAggregate(
            @NonNull String instanceId,
            @NonNull String infoCode) {
        InfoCodeAggregate aggregate = Mockito.mock(InfoCodeAggregate.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(aggregate.getIdentifier().getObject().getId()).thenReturn(instanceId);
        Mockito.when(aggregate.getConfigModel().getInfoCode()).thenReturn(infoCode);
        return aggregate;
    }

    /**
     * Build a valid precomputed result with multiple seasonal values.
     *
     * @return configured result
     * @since v7.260701.0
     */
    private @NonNull DemandConfigResultVO<DemandConfigAirconditioningValueObject> buildConfiguredResult() {
        DemandConfigAirconditioningTdbSeasonValueObject summerTdb =
                new DemandConfigAirconditioningTdbSeasonValueObject();
        summerTdb.setAirSeason(MidCalendarAirSeasonTypeEnum.AIR_SUMMER);
        summerTdb.setLowerComfortSeasonLimitValue(new BigDecimal("24"));
        DemandConfigAirconditioningTdbSeasonValueObject winterTdb =
                new DemandConfigAirconditioningTdbSeasonValueObject();
        winterTdb.setAirSeason(MidCalendarAirSeasonTypeEnum.AIR_WINTER);
        winterTdb.setLowerComfortSeasonLimitValue(new BigDecimal("18"));
        DemandConfigAirconditioningTdbQualityValueObject tdbQuality =
                new DemandConfigAirconditioningTdbQualityValueObject();
        tdbQuality.setSeasonQuality(Arrays.asList(winterTdb, summerTdb));

        DemandConfigAirconditioningRhSeasonValueObject summerRh =
                new DemandConfigAirconditioningRhSeasonValueObject();
        summerRh.setAirSeason(MidCalendarAirSeasonTypeEnum.AIR_SUMMER);
        summerRh.setLowerComfortSeasonLimitValue(new BigDecimal("40"));
        DemandConfigAirconditioningRhSeasonValueObject winterRh =
                new DemandConfigAirconditioningRhSeasonValueObject();
        winterRh.setAirSeason(MidCalendarAirSeasonTypeEnum.AIR_WINTER);
        winterRh.setLowerComfortSeasonLimitValue(new BigDecimal("30"));
        DemandConfigAirconditioningRhQualityValueObject rhQuality =
                new DemandConfigAirconditioningRhQualityValueObject();
        rhQuality.setSeasonQuality(Arrays.asList(winterRh, summerRh));

        DemandConfigAirconditioningCO2QualityValueObject co2Quality =
                new DemandConfigAirconditioningCO2QualityValueObject();
        co2Quality.setCarbonDioxideLimitValue(new BigDecimal("800"));
        DemandConfigAirconditioningPressureDiffQualityValueObject pressureDiffQuality =
                new DemandConfigAirconditioningPressureDiffQualityValueObject();
        pressureDiffQuality.setLowerLimitValue(new BigDecimal("5"));

        DemandConfigAirconditioningEnvironmentQualityObject environmentQuality =
                new DemandConfigAirconditioningEnvironmentQualityObject();
        environmentQuality.setTdbQuality(tdbQuality);
        environmentQuality.setRhQuality(rhQuality);
        environmentQuality.setCO2Quality(co2Quality);
        environmentQuality.setIsPressureDiffQualityEnabled(true);
        environmentQuality.setPressureDiffQuality(pressureDiffQuality);

        DemandConfigAirconditioningValueObject globalConfig =
                new DemandConfigAirconditioningValueObject();
        globalConfig.setEnvironmentQuality(environmentQuality);
        DemandConfigValueObject<DemandConfigAirconditioningValueObject> finalConfig = new DemandConfigValueObject<>();
        finalConfig.setGlobalConfig(globalConfig);
        return this.buildResult(CONFIGURED_SPACE_ID, finalConfig);
    }

    /**
     * Build a result with no final configuration.
     *
     * @return empty result
     * @since v7.260701.0
     */
    private @NonNull DemandConfigResultVO<DemandConfigAirconditioningValueObject> buildEmptyConfigResult() {
        return this.buildResult(EMPTY_CONFIG_SPACE_ID, null);
    }

    /**
     * Build a lightweight demand-config result.
     *
     * @param instanceId instance ID
     * @param finalConfig final configuration
     * @return result VO
     * @since v7.260701.0
     */
    private @NonNull DemandConfigResultVO<DemandConfigAirconditioningValueObject> buildResult(
            @NonNull String instanceId,
            @Nullable DemandConfigValueObject<DemandConfigAirconditioningValueObject> finalConfig) {
        DemandConfigResultVO<DemandConfigAirconditioningValueObject> result = new DemandConfigResultVO<>();
        result.setInstanceId(instanceId);
        result.setFinalConfig(finalConfig);
        return result;
    }

    /**
     * Assert one aggregate receives the expected mapped decimal field.
     *
     * @param aggregate aggregate mock
     * @param fieldName JSON field name
     * @param expected expected decimal value
     * @since v7.260701.0
     */
    private void assertMappedDecimal(
            @NonNull InfoCodeAggregate aggregate,
            @NonNull String fieldName,
            @NonNull String expected) {
        ArgumentCaptor<InfoCodeDataModel> dataCaptor = ArgumentCaptor.forClass(InfoCodeDataModel.class);
        Mockito.verify(aggregate, Mockito.times(1)).setInfoCodeAndDataModel(dataCaptor.capture());
        JSONObject dataValue = (JSONObject) dataCaptor.getValue().getDataValue();
        Assertions.assertEquals(new BigDecimal(expected), dataValue.getBigDecimal(fieldName));
    }
}
