package com.persagy.apccontrol.basic.structure.runanalysis.strategy.impl;

import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataSourceDefineModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportChapterEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.RunAnalysisReportDataDto;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.common.ExceptionRecordDTO;
import com.persagy.apccontrol.basic.utils.i18n.LocaleContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

class AbstractRunAnalysisReportStrategyTest {

    private static final String OBJECT_ID = "chiller-2";
    private static final String CLASS_CODE = "dual-condition-chiller";
    private static final String INFO_CODE = "valve-control-disabled-alarm";
    private final TestRunAnalysisReportStrategy strategy = new TestRunAnalysisReportStrategy();

    @BeforeAll
    static void initLocaleContext() {
        LocaleContext.setLocaleContext(Locale.ENGLISH);
    }

    @AfterAll
    static void clearLocaleContext() {
        LocaleContext.remove();
    }

    @Test
    void shouldMergeContinuousUnrecoveredExceptionValues() throws Exception {
        LocalDateTime triggerTime = LocalDateTime.of(2026, 6, 7, 23, 30);

        List<ExceptionRecordDTO> records = this.buildExceptionRecords(Arrays.asList(object()), Arrays.asList(
                value("1", triggerTime),
                value("1", triggerTime.plusMinutes(15))
        ));

        Assertions.assertEquals(1, records.size());
        Assertions.assertEquals(triggerTime, records.get(0).getTriggerTime());
        Assertions.assertNull(records.get(0).getRecoverTime());
        Assertions.assertEquals("1", records.get(0).getInfoCodeValue());
    }

    @Test
    void shouldSetRecoverTimeAfterContinuousExceptionValues() throws Exception {
        LocalDateTime triggerTime = LocalDateTime.of(2026, 6, 7, 23, 30);
        LocalDateTime recoverTime = triggerTime.plusMinutes(30);

        List<ExceptionRecordDTO> records = this.buildExceptionRecords(Arrays.asList(object()), Arrays.asList(
                value("1", triggerTime),
                value("1", triggerTime.plusMinutes(15)),
                value("0", recoverTime)
        ));

        Assertions.assertEquals(1, records.size());
        Assertions.assertEquals(triggerTime, records.get(0).getTriggerTime());
        Assertions.assertEquals(recoverTime, records.get(0).getRecoverTime());
        Assertions.assertEquals("1", records.get(0).getInfoCodeValue());
    }

    @Test
    void shouldIgnoreDuplicateSamplesAtSameTimeAndValue() throws Exception {
        LocalDateTime triggerTime = LocalDateTime.of(2026, 6, 7, 23, 30);
        LocalDateTime recoverTime = triggerTime.plusMinutes(30);

        List<ExceptionRecordDTO> records = this.buildExceptionRecords(Arrays.asList(object()), Arrays.asList(
                value("1", triggerTime),
                value("1", triggerTime),
                value("1", triggerTime.plusMinutes(15)),
                value("0", recoverTime)
        ));

        Assertions.assertEquals(1, records.size());
        Assertions.assertEquals(triggerTime, records.get(0).getTriggerTime());
        Assertions.assertEquals(recoverTime, records.get(0).getRecoverTime());
    }

    @Test
    void shouldKeepExceptionRecordAfterRecoveredPeriod() throws Exception {
        LocalDateTime firstTriggerTime = LocalDateTime.of(2026, 6, 7, 23, 0);
        LocalDateTime firstRecoverTime = firstTriggerTime.plusMinutes(30);
        LocalDateTime secondTriggerTime = firstTriggerTime.plusMinutes(45);

        List<ExceptionRecordDTO> records = this.buildExceptionRecords(Arrays.asList(object()), Arrays.asList(
                value("1", firstTriggerTime),
                value("1", firstTriggerTime.plusMinutes(15)),
                value("0", firstRecoverTime),
                value("1", secondTriggerTime)
        ));

        Assertions.assertEquals(2, records.size());
        Assertions.assertEquals(firstTriggerTime, records.get(0).getTriggerTime());
        Assertions.assertEquals(firstRecoverTime, records.get(0).getRecoverTime());
        Assertions.assertEquals(secondTriggerTime, records.get(1).getTriggerTime());
        Assertions.assertNull(records.get(1).getRecoverTime());
    }

    @Test
    void shouldMergeContinuousExceptionValuesEvenWhenAbnormalValueChanges() throws Exception {
        LocalDateTime triggerTime = LocalDateTime.of(2026, 6, 7, 23, 0);
        LocalDateTime recoverTime = triggerTime.plusMinutes(45);

        List<ExceptionRecordDTO> records = this.buildExceptionRecords(Arrays.asList(object()), Arrays.asList(
                value("1", triggerTime),
                value("1", triggerTime.plusMinutes(15)),
                value("2", triggerTime.plusMinutes(30)),
                value("0", recoverTime)
        ));

        Assertions.assertEquals(1, records.size());
        Assertions.assertEquals(triggerTime, records.get(0).getTriggerTime());
        Assertions.assertEquals(recoverTime, records.get(0).getRecoverTime());
        Assertions.assertEquals("1", records.get(0).getInfoCodeValue());
    }

    private List<ExceptionRecordDTO> buildExceptionRecords(
            List<DtpRwdObjectDTO> objects, List<InfoCodeValueVO> values) throws Exception {
        Method method = AbstractRunAnalysisReportStrategy.class.getDeclaredMethod(
                "buildExceptionRecords", List.class, List.class, Map.class);
        method.setAccessible(true);
        List<?> records = (List<?>) method.invoke(this.strategy, objects, values, infoCodeDefines());
        return records.stream()
                .map(ExceptionRecordDTO.class::cast)
                .collect(Collectors.toList());
    }

    private static DtpRwdObjectDTO object() {
        DtpRwdObjectDTO object = new DtpRwdObjectDTO();
        object.setId(OBJECT_ID);
        object.setName("Dual Condition Chiller 2");
        object.setClassCode(CLASS_CODE);
        return object;
    }

    private static InfoCodeValueVO value(String infoCodeValue, LocalDateTime dataTime) {
        return new InfoCodeValueVO()
                .setObjectId(OBJECT_ID)
                .setInfoCode(INFO_CODE)
                .setInfoCodeValue(infoCodeValue)
                .setDataTime(dataTime);
    }

    private static Map<String, List<InfoCodeDefineModel>> infoCodeDefines() {
        InfoCodeDefineModel define = new InfoCodeDefineModel();
        define.setCode(INFO_CODE);
        define.setName("Valve Control Disabled Alarm");
        define.setClassCode(CLASS_CODE);
        define.setDataSources(Arrays.asList(
                new InfoCodeDataSourceDefineModel("0", "Normal"),
                new InfoCodeDataSourceDefineModel("1", "Alarm"),
                new InfoCodeDataSourceDefineModel("2", "Trip")
        ));
        return Collections.singletonMap(CLASS_CODE, Collections.singletonList(define));
    }

    private static class TestRunAnalysisReportStrategy
            extends AbstractRunAnalysisReportStrategy<RunAnalysisReportDataDto> {

        @Override
        public Boolean isSupport(RunAnalysisReportTypeEnum type) {
            return false;
        }

        @Override
        public RunAnalysisReportChapterEnum getChapter() {
            return RunAnalysisReportChapterEnum.REPORT_CHAPTER_COOLING_SYS_RUN;
        }

        @Override
        public String logTag() {
            return "AbstractRunAnalysisReportStrategyTest";
        }
    }
}
