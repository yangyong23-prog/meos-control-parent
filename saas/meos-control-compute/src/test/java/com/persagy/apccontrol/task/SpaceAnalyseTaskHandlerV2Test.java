package com.persagy.apccontrol.task;

import com.persagy.apccontrol.basic.model.entity.AnalysisAirConditionerSpace;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalTemperatureIntervalEnum;
import com.persagy.apccontrol.basic.model.po.common.TimeTagPO;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningEnvironmentQualityObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbQualityValueObject;
import org.apache.commons.lang3.Range;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Tests for space analysis task handler.
 *
 * @since v7.260401.0
 */
public class SpaceAnalyseTaskHandlerV2Test {

    @Test
    public void computeQualifyRateShouldStopWhenBacktrackingToMidnight() throws Exception {
        AnalysisAirConditionerSpace qualifySpace = Assertions.assertTimeoutPreemptively(Duration.ofSeconds(1),
                () -> computeQualifyRate(LocalTime.MIN, LocalTime.of(1, 0), new BigDecimal("1"),
                        Arrays.asList(timeTag(LocalTime.MIN), timeTag(LocalTime.of(0, 15)))));

        Assertions.assertEquals(Integer.valueOf(1), qualifySpace.getTimeNumber());
        Assertions.assertEquals(Integer.valueOf(1), qualifySpace.getSatisfyTimeNumber());
        Assertions.assertEquals(AirTerminalTemperatureIntervalEnum.APPROPRIATE.value(), qualifySpace.getTimeTag().get(0).getTag());
    }

    @Test
    public void computeQualifyRateShouldKeepContinuousThresholdAtMidnight() throws Exception {
        AnalysisAirConditionerSpace qualifySpace = Assertions.assertTimeoutPreemptively(Duration.ofSeconds(1),
                () -> computeQualifyRate(LocalTime.MIN, LocalTime.of(1, 0), new BigDecimal("0.25"),
                        Arrays.asList(timeTag(LocalTime.MIN), timeTag(LocalTime.of(0, 15)))));

        Assertions.assertEquals(Integer.valueOf(1), qualifySpace.getTimeNumber());
        Assertions.assertEquals(Integer.valueOf(0), qualifySpace.getSatisfyTimeNumber());
        Assertions.assertEquals(hotTag(), qualifySpace.getTimeTag().get(0).getTag());
    }

    @Test
    public void computeQualifyRateShouldNotUseMidnightDataBeforeBusinessStart() throws Exception {
        AnalysisAirConditionerSpace qualifySpace = Assertions.assertTimeoutPreemptively(Duration.ofSeconds(1),
                () -> computeQualifyRate(LocalTime.of(0, 1), LocalTime.of(1, 0), new BigDecimal("0.25"),
                        Arrays.asList(timeTag(LocalTime.MIN), timeTag(LocalTime.of(0, 15)))));

        Assertions.assertEquals(Integer.valueOf(1), qualifySpace.getTimeNumber());
        Assertions.assertEquals(Integer.valueOf(1), qualifySpace.getSatisfyTimeNumber());
        Assertions.assertEquals(AirTerminalTemperatureIntervalEnum.APPROPRIATE.value(), qualifySpace.getTimeTag().get(0).getTag());
    }

    @Test
    public void computeQualifyRateShouldTreatMidnightAsEndOfDayForLastHour() throws Exception {
        AnalysisAirConditionerSpace qualifySpace = Assertions.assertTimeoutPreemptively(Duration.ofSeconds(1),
                () -> computeQualifyRate(LocalTime.MIN, LocalTime.MAX, new BigDecimal("0.25"),
                        Arrays.asList(timeTag(LocalTime.of(23, 45)), timeTag(LocalTime.MIN))));

        Assertions.assertEquals(Integer.valueOf(1), qualifySpace.getTimeNumber());
        Assertions.assertEquals(Integer.valueOf(0), qualifySpace.getSatisfyTimeNumber());
        Assertions.assertEquals(hotTag(), qualifySpace.getTimeTag().get(0).getTag());
    }

    private AnalysisAirConditionerSpace computeQualifyRate(LocalTime businessStart, LocalTime businessEnd,
                                                           BigDecimal continuousScheduledHour,
                                                           List<TimeTagPO> timeTags) throws Exception {
        SpaceAnalyseTaskHandlerV2 handler = new SpaceAnalyseTaskHandlerV2(null, null, null, null,
                null, null, null, null, null);
        AnalysisAirConditionerSpace space = buildAnalysisSpace(businessStart, businessEnd, timeTags);
        AnalysisAirConditionerSpace qualifySpace = buildQualifySpace();
        DemandConfigAirconditioningEnvironmentQualityObject environmentQuality = buildEnvironmentQuality(continuousScheduledHour);

        Method method = SpaceAnalyseTaskHandlerV2.class.getDeclaredMethod("computeQualifyRate",
                AnalysisAirConditionerSpace.class,
                AnalysisAirConditionerSpace.class,
                DemandConfigAirconditioningEnvironmentQualityObject.class);
        method.setAccessible(true);
        method.invoke(handler, space, qualifySpace, environmentQuality);
        return qualifySpace;
    }

    private AnalysisAirConditionerSpace buildAnalysisSpace(LocalTime businessStart, LocalTime businessEnd,
                                                           List<TimeTagPO> timeTags) {
        LinkedHashSet<Range<LocalTime>> businessTime = new LinkedHashSet<>();
        businessTime.add(Range.between(businessStart, businessEnd));

        return AnalysisAirConditionerSpace.builder()
                .tagType(RunningMonitorSecondTypeEnum.RUN_ANALYSIS_TDB)
                .businessTime(businessTime)
                .timeTag(timeTags)
                .build();
    }

    private AnalysisAirConditionerSpace buildQualifySpace() {
        return AnalysisAirConditionerSpace.builder()
                .tagType(RunningMonitorSecondTypeEnum.RUN_QUALIFY_TDB)
                .build();
    }

    private DemandConfigAirconditioningEnvironmentQualityObject buildEnvironmentQuality(BigDecimal continuousScheduledHour) {
        DemandConfigAirconditioningTdbQualityValueObject tdbQuality = new DemandConfigAirconditioningTdbQualityValueObject();
        tdbQuality.setTemperatureContinuousScheduledHour(continuousScheduledHour);

        DemandConfigAirconditioningEnvironmentQualityObject environmentQuality = new DemandConfigAirconditioningEnvironmentQualityObject();
        environmentQuality.setIsParticipateInScheduling(true);
        environmentQuality.setTdbQuality(tdbQuality);
        return environmentQuality;
    }

    private TimeTagPO timeTag(LocalTime time) {
        return TimeTagPO.builder()
                .time(Range.between(time, time))
                .tag(hotTag())
                .build();
    }

    private String hotTag() {
        return AirTerminalTemperatureIntervalEnum.HOT.value() + ":1";
    }
}
