package com.persagy.apccontrol.basic.structure.demand.lifecycle.config;

import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.dto.calendar.MidCalendarDTO;
import com.persagy.apccontrol.basic.model.dto.calendar.MidCalendarGlobalRuleDTO;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.DemandConfigAirconditioningCustomDTO;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagValueEnum;
import com.persagy.apccontrol.basic.model.vo.demand.DemandConfigResultVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.MidCalendarDayDateTagQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.calendar.MidCalendarDayDateTagResponse;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigAirconditioningCustomService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningEnvironmentQualityObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbQualityValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbSeasonValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueSelectValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningWorkConditionValueObject;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.airconditioning.DemandConfigAirConditioningAnalyzeStrategy;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import org.apache.commons.lang3.Range;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for air-conditioning query-time demand-config enrichment.
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class DemandConfigAirconditioningQueryEnricherTest {

    /**
     * Current project used by query-time calendar lookup.
     *
     * @since v7.260701.0
     */
    private static final String PROJECT_ID = "project-id";

    /**
     * Resolved demand-template ID used by result enrichment.
     *
     * @since v7.260701.0
     */
    private static final Long TEMPLATE_ID = 101L;

    /**
     * Stable historical date that always follows the day-tag query path.
     *
     * @since v7.260701.0
     */
    private static final LocalDate HISTORY_DATE = LocalDate.of(2000, 1, 15);

    /**
     * Mocked calendar dependency.
     *
     * @since v7.260701.0
     */
    private MidCalendarClientService midCalendarClientService;

    /**
     * Mocked project custom-config dependency.
     *
     * @since v7.260701.0
     */
    private DemandConfigAirconditioningCustomService customService;

    /**
     * Mocked air-conditioning configuration strategy.
     *
     * @since v7.260701.0
     */
    private DemandConfigAirConditioningAnalyzeStrategy configAnalyzeStrategy;

    /**
     * Query enricher under test.
     *
     * @since v7.260701.0
     */
    private DemandConfigAirconditioningQueryEnricher enricher;

    /**
     * Initialize mocked dependencies and the current-project context.
     *
     * @since v7.260701.0
     */
    @BeforeEach
    public void setUp() {
        ContextUtil.setProjectId(DemandConfigAirconditioningQueryEnricherTest.PROJECT_ID);
        this.midCalendarClientService = Mockito.mock(MidCalendarClientService.class);
        this.customService = Mockito.mock(DemandConfigAirconditioningCustomService.class);
        this.configAnalyzeStrategy = Mockito.mock(DemandConfigAirConditioningAnalyzeStrategy.class);
        Mockito.when(this.configAnalyzeStrategy.defaultConfig(DemandInstanceTypeEnum.PROJECT))
                .thenReturn(DemandConfigAirconditioningQueryEnricherTest::buildDefaultConfig);
        this.enricher = new DemandConfigAirconditioningQueryEnricher(
                this.midCalendarClientService, this.customService, this.configAnalyzeStrategy);
        Mockito.when(this.midCalendarClientService.listCalendarDayDateTag(
                        Mockito.eq(DemandConfigAirconditioningQueryEnricherTest.PROJECT_ID),
                        Mockito.any(MidCalendarDayDateTagQueryParam.class)))
                .thenReturn(Collections.emptyList());
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
     * Verify historical day tags filter seasons while retaining matching customized values.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldFilterHistoricalSeasonQualityAndPreserveCustomizedValues() {
        DemandConfigAirconditioningTdbSeasonValueObject customWinter = this.buildSeasonConfig(
                MidCalendarAirSeasonTypeEnum.AIR_WINTER, new BigDecimal("17.5"));
        DemandConfigAirconditioningTdbSeasonValueObject customSpring = this.buildSeasonConfig(
                MidCalendarAirSeasonTypeEnum.AIR_SEASON_SPRING, new BigDecimal("20.5"));
        DemandConfigAirconditioningTdbSeasonValueObject customSummer = this.buildSeasonConfig(
                MidCalendarAirSeasonTypeEnum.AIR_SUMMER, new BigDecimal("23.5"));
        DemandConfigAirconditioningValueObject globalConfig = this.buildAirconditioningConfig(
                Arrays.asList(customWinter, customSpring, customSummer));
        DemandConfigResultVO<BaseDemandConfig> result = this.buildResult("space-1", globalConfig);
        MidCalendarDayDateTagResponse response = new MidCalendarDayDateTagResponse();
        response.setTagList(Arrays.asList(
                MidCalendarAirSeasonTypeEnum.AIR_WINTER.getValue(),
                MidCalendarAirSeasonTypeEnum.AIR_SUMMER.getValue(),
                "BusinessDay"));
        Mockito.when(this.midCalendarClientService.listCalendarDayDateTag(
                        Mockito.eq(DemandConfigAirconditioningQueryEnricherTest.PROJECT_ID),
                        Mockito.any(MidCalendarDayDateTagQueryParam.class)))
                .thenReturn(Collections.singletonList(response));
        Mockito.clearInvocations(this.configAnalyzeStrategy);

        this.enricher.enrichResults(
                Collections.singletonList(result),
                DemandTemplateTypeEnum.AIRCONDITIONING,
                DemandConfigAirconditioningQueryEnricherTest.TEMPLATE_ID,
                DemandInstanceTypeEnum.SPACE,
                DemandConfigAirconditioningQueryEnricherTest.HISTORY_DATE);

        List<DemandConfigAirconditioningTdbSeasonValueObject> actualSeasons =
                globalConfig.getEnvironmentQuality().getTdbQuality().getSeasonQuality();
        Assertions.assertEquals(2, actualSeasons.size());
        Assertions.assertSame(customSummer, actualSeasons.get(0));
        Assertions.assertEquals(new BigDecimal("23.5"),
                actualSeasons.get(0).getLowerComfortSeasonLimitValue());
        Assertions.assertSame(customWinter, actualSeasons.get(1));
        Assertions.assertEquals(new BigDecimal("17.5"),
                actualSeasons.get(1).getLowerComfortSeasonLimitValue());

        ArgumentCaptor<MidCalendarDayDateTagQueryParam> paramCaptor =
                ArgumentCaptor.forClass(MidCalendarDayDateTagQueryParam.class);
        Mockito.verify(this.midCalendarClientService).listCalendarDayDateTag(
                Mockito.eq(DemandConfigAirconditioningQueryEnricherTest.PROJECT_ID),
                paramCaptor.capture());
        Assertions.assertEquals(DemandConfigAirconditioningQueryEnricherTest.PROJECT_ID,
                paramCaptor.getValue().getObjId());
        Assertions.assertEquals(DemandConfigAirconditioningQueryEnricherTest.HISTORY_DATE,
                paramCaptor.getValue().getBeginDate());
        Assertions.assertEquals(DemandConfigAirconditioningQueryEnricherTest.HISTORY_DATE,
                paramCaptor.getValue().getEndDate());
        Mockito.verifyNoMoreInteractions(this.midCalendarClientService);
        Mockito.verifyNoInteractions(this.customService);
    }

    /**
     * Verify current and future result queries both use the current global calendar rule.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldUseGlobalCalendarRuleForCurrentAndFutureDates() {
        MidCalendarDTO projectCalendar = new MidCalendarDTO();
        projectCalendar.setCalendarId("calendar-id");
        MidCalendarGlobalRuleDTO globalRule = new MidCalendarGlobalRuleDTO();
        LinkedHashSet<Range<MonthDay>> summerRanges = new LinkedHashSet<>();
        summerRanges.add(Range.between(MonthDay.of(1, 1), MonthDay.of(12, 31)));
        Map<MidCalendarDateTagValueEnum, LinkedHashSet<Range<MonthDay>>> airSeasonRanges =
                new LinkedHashMap<>();
        airSeasonRanges.put(MidCalendarDateTagValueEnum.AIR_SUMMER, summerRanges);
        globalRule.setGlobalAirSeasonRangeDateRangeMap(airSeasonRanges);
        Mockito.when(this.midCalendarClientService.getCalendar(
                        Mockito.eq(DemandConfigAirconditioningQueryEnricherTest.PROJECT_ID),
                        Mockito.any()))
                .thenReturn(projectCalendar);
        Mockito.when(this.midCalendarClientService.getGlobalCalendarRule(
                        DemandConfigAirconditioningQueryEnricherTest.PROJECT_ID,
                        projectCalendar.getCalendarId(), null, null))
                .thenReturn(globalRule);
        List<@Nullable LocalDate> queryDates = Arrays.asList(
                null, DateUtils.now().toLocalDate().plusDays(1));

        for (@Nullable LocalDate queryDate : queryDates) {
            DemandConfigAirconditioningTdbSeasonValueObject customWinter = this.buildSeasonConfig(
                    MidCalendarAirSeasonTypeEnum.AIR_WINTER, new BigDecimal("17.5"));
            DemandConfigAirconditioningTdbSeasonValueObject customSummer = this.buildSeasonConfig(
                    MidCalendarAirSeasonTypeEnum.AIR_SUMMER, new BigDecimal("23.5"));
            DemandConfigAirconditioningValueObject globalConfig = this.buildAirconditioningConfig(
                    Arrays.asList(customWinter, customSummer));
            DemandConfigResultVO<BaseDemandConfig> result = this.buildResult("space-1", globalConfig);

            this.enricher.enrichResults(
                    Collections.singletonList(result),
                    DemandTemplateTypeEnum.AIRCONDITIONING,
                    DemandConfigAirconditioningQueryEnricherTest.TEMPLATE_ID,
                    DemandInstanceTypeEnum.SPACE,
                    queryDate);

            List<DemandConfigAirconditioningTdbSeasonValueObject> actualSeasons =
                    globalConfig.getEnvironmentQuality().getTdbQuality().getSeasonQuality();
            Assertions.assertEquals(1, actualSeasons.size());
            Assertions.assertSame(customSummer, actualSeasons.get(0));
        }

        Mockito.verify(this.midCalendarClientService, Mockito.times(queryDates.size()))
                .getCalendar(
                        Mockito.eq(DemandConfigAirconditioningQueryEnricherTest.PROJECT_ID),
                        Mockito.any());
        Mockito.verify(this.midCalendarClientService, Mockito.times(queryDates.size()))
                .getGlobalCalendarRule(
                        DemandConfigAirconditioningQueryEnricherTest.PROJECT_ID,
                        projectCalendar.getCalendarId(), null, null);
        Mockito.verify(this.midCalendarClientService, Mockito.never())
                .listCalendarDayDateTag(Mockito.anyString(), Mockito.any());
        Mockito.verifyNoInteractions(this.customService);
    }

    /**
     * Verify project results skip null-config placeholders before filling dynamic custom data.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldSkipPlaceholderAndFillProjectResultCustomConfig() {
        DemandConfigAirconditioningValueObject globalConfig =
                new DemandConfigAirconditioningValueObject();
        DemandConfigResultVO<BaseDemandConfig> placeholder = new DemandConfigResultVO<>();
        placeholder.setInstanceId("blacklisted-project");
        DemandConfigResultVO<BaseDemandConfig> result =
                this.buildResult("result-project-id", globalConfig);
        DemandConfigAirconditioningWorkConditionValueObject workCondition =
                new DemandConfigAirconditioningWorkConditionValueObject();
        DemandConfigAirconditioningCustomDTO customConfig =
                new DemandConfigAirconditioningCustomDTO();
        customConfig.setValueSelectType(BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_ENERGY);
        customConfig.setWorkCondition(workCondition);
        Mockito.when(this.customService.getConfigData(
                        DemandConfigAirconditioningQueryEnricherTest.PROJECT_ID,
                        DemandConfigAirconditioningQueryEnricherTest.TEMPLATE_ID.toString(),
                        DemandConfigAirconditioningQueryEnricherTest.HISTORY_DATE))
                .thenReturn(customConfig);

        this.enricher.enrichResults(
                Arrays.asList(placeholder, result),
                DemandTemplateTypeEnum.AIRCONDITIONING,
                DemandConfigAirconditioningQueryEnricherTest.TEMPLATE_ID,
                DemandInstanceTypeEnum.PROJECT,
                DemandConfigAirconditioningQueryEnricherTest.HISTORY_DATE);

        Assertions.assertNull(placeholder.getFinalConfig());
        Assertions.assertNotNull(globalConfig.getValueSelect());
        Assertions.assertEquals(BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_ENERGY,
                globalConfig.getValueSelect().getValueSelectType());
        Assertions.assertSame(workCondition, globalConfig.getWorkCondition());
        Mockito.verify(this.customService).getConfigData(
                DemandConfigAirconditioningQueryEnricherTest.PROJECT_ID,
                DemandConfigAirconditioningQueryEnricherTest.TEMPLATE_ID.toString(),
                DemandConfigAirconditioningQueryEnricherTest.HISTORY_DATE);
        Mockito.verifyNoMoreInteractions(this.customService);
    }

    /**
     * Verify space result enrichment never queries project custom configuration.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldNotQueryProjectCustomConfigForSpaceResult() {
        DemandConfigAirconditioningValueObject globalConfig =
                new DemandConfigAirconditioningValueObject();
        DemandConfigAirconditioningValueSelectValueObject originalValueSelect =
                new DemandConfigAirconditioningValueSelectValueObject(
                        BasicsConfigTypeEnum.VALUE_SELECT_QUALITY_FIRST);
        globalConfig.setValueSelect(originalValueSelect);
        DemandConfigResultVO<BaseDemandConfig> result = this.buildResult("space-1", globalConfig);

        this.enricher.enrichResults(
                Collections.singletonList(result),
                DemandTemplateTypeEnum.AIRCONDITIONING,
                DemandConfigAirconditioningQueryEnricherTest.TEMPLATE_ID,
                DemandInstanceTypeEnum.SPACE,
                DemandConfigAirconditioningQueryEnricherTest.HISTORY_DATE);

        Assertions.assertSame(originalValueSelect, globalConfig.getValueSelect());
        Mockito.verifyNoInteractions(this.customService);
    }

    /**
     * Verify non-air-conditioning templates bypass enrichment and all collaborators.
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldSkipNonAirconditioningTemplateWithoutExternalAccess() {
        DemandConfigAirconditioningValueObject globalConfig =
                new DemandConfigAirconditioningValueObject();
        DemandConfigAirconditioningValueSelectValueObject originalValueSelect =
                new DemandConfigAirconditioningValueSelectValueObject(
                        BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_CAST);
        globalConfig.setValueSelect(originalValueSelect);
        DemandConfigResultVO<BaseDemandConfig> result = this.buildResult(
                DemandConfigAirconditioningQueryEnricherTest.PROJECT_ID, globalConfig);

        this.enricher.enrichResults(
                Collections.singletonList(result),
                DemandTemplateTypeEnum.PUBLIC_AREA_LIGHTING,
                DemandConfigAirconditioningQueryEnricherTest.TEMPLATE_ID,
                DemandInstanceTypeEnum.PROJECT,
                DemandConfigAirconditioningQueryEnricherTest.HISTORY_DATE);

        Assertions.assertSame(originalValueSelect, globalConfig.getValueSelect());
        Mockito.verifyNoInteractions(
                this.midCalendarClientService,
                this.customService,
                this.configAnalyzeStrategy);
    }

    /**
     * Build the default project configuration used to order supported seasons.
     *
     * @return default air-conditioning configuration
     * @since v7.260701.0
     */
    private static @NonNull DemandConfigAirconditioningValueObject buildDefaultConfig() {
        DemandConfigAirconditioningValueObject defaultConfig =
                new DemandConfigAirconditioningValueObject();
        defaultConfig.fillDefaultConfig(DemandInstanceTypeEnum.PROJECT);
        return defaultConfig;
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

    /**
     * Build a lightweight result around a real air-conditioning value object.
     *
     * @param instanceId result instance ID
     * @param globalConfig air-conditioning global configuration
     * @return lightweight result VO
     * @since v7.260701.0
     */
    private @NonNull DemandConfigResultVO<BaseDemandConfig> buildResult(
            @NonNull String instanceId,
            @NonNull DemandConfigAirconditioningValueObject globalConfig) {
        DemandConfigValueObject<BaseDemandConfig> finalConfig = new DemandConfigValueObject<>();
        finalConfig.setGlobalConfig(globalConfig);
        DemandConfigResultVO<BaseDemandConfig> result = new DemandConfigResultVO<>();
        result.setInstanceId(instanceId);
        result.setFinalConfig(finalConfig);
        return result;
    }
}
