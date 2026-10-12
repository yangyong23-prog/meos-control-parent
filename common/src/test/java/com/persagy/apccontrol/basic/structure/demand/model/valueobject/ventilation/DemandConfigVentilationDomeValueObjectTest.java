package com.persagy.apccontrol.basic.structure.demand.model.valueobject.ventilation;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTimeTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeValueObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

/**
 * 通风需求-穹顶配置值对象自然通风需求时间字段测试。
 *
 * @author JiYinchuan
 * @since v7.260802.2
 */
class DemandConfigVentilationDomeValueObjectTest {

    /**
     * 反序列化用 ObjectMapper, 通过 Spring 上下文提供给 {@code readValueFromMap}。
     *
     * @since v7.260802.2
     */
    private static ObjectMapper objectMapper;

    /**
     * 原始 Spring 上下文, 测试后恢复。
     *
     * @since v7.260802.2
     */
    private static ApplicationContext originalApplicationContext;

    /**
     * 注册反序列化所需的 ObjectMapper Bean, 对齐 Spring Boot 默认的 JavaTime 行为。
     *
     * @since v7.260802.2
     */
    @BeforeAll
    static void setUpSpringUtil() {
        DemandConfigVentilationDomeValueObjectTest.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        DemandConfigVentilationDomeValueObjectTest.originalApplicationContext = SpringUtil.getApplicationContext();
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.getBeanFactory().registerSingleton("objectMapper",
                DemandConfigVentilationDomeValueObjectTest.objectMapper);
        applicationContext.refresh();
        new SpringUtil().setApplicationContext(applicationContext);
    }

    /**
     * 恢复原始 Spring 上下文。
     *
     * @since v7.260802.2
     */
    @AfterAll
    static void tearDownSpringUtil() {
        new SpringUtil().setApplicationContext(DemandConfigVentilationDomeValueObjectTest.originalApplicationContext);
    }

    /**
     * 反序列化应绑定自然通风需求时间并保持时段顺序。
     *
     * @since v7.260802.2
     */
    @Test
    void deserializeShouldBindNaturalVentilationTimeIntervalsAndKeepOrder() throws Exception {
        DemandConfigVentilationDomeValueObject config = this.deserialize(
                "{\n"
                        + "  \"tdbTarget\": 32.5,\n"
                        + "  \"participateFeedbackAdjust\": true,\n"
                        + "  \"naturalVentilationTimeIntervals\": [\n"
                        + "    {\"beginTime\": {\"timeType\": \"ABSOLUTE\", \"absoluteTime\": \"08:00:00\"},\n"
                        + "     \"endTime\": {\"timeType\": \"ABSOLUTE\", \"absoluteTime\": \"12:00:00\"}},\n"
                        + "    {\"beginTime\": {\"timeType\": \"ABSOLUTE\", \"absoluteTime\": \"14:00:00\"},\n"
                        + "     \"endTime\": {\"timeType\": \"ABSOLUTE\", \"absoluteTime\": \"18:30:00\"}, \"acrossDay\": true}\n"
                        + "  ]\n"
                        + "}");

        Assertions.assertNotNull(config.getTdbTarget());
        Assertions.assertEquals(0, config.getTdbTarget().compareTo(new BigDecimal("32.5")));
        Assertions.assertEquals(Boolean.TRUE, config.getParticipateFeedbackAdjust());
        List<DemandTimeIntervalValueObject> timeIntervals = config.getNaturalVentilationTimeIntervals();
        Assertions.assertNotNull(timeIntervals);
        Assertions.assertEquals(2, timeIntervals.size());
        Assertions.assertEquals(LocalTime.of(8, 0), timeIntervals.get(0).getBeginTime().getAbsoluteTime());
        Assertions.assertEquals(LocalTime.of(12, 0), timeIntervals.get(0).getEndTime().getAbsoluteTime());
        Assertions.assertEquals(LocalTime.of(14, 0), timeIntervals.get(1).getBeginTime().getAbsoluteTime());
        Assertions.assertEquals(LocalTime.of(18, 30), timeIntervals.get(1).getEndTime().getAbsoluteTime());
        Assertions.assertEquals(Boolean.TRUE, timeIntervals.get(1).getAcrossDay());
    }

    /**
     * 序列化回写应保留自然通风需求时间, 且可再次反序列化还原。
     *
     * @since v7.260802.2
     */
    @Test
    void serializeShouldKeepNaturalVentilationTimeIntervals() throws Exception {
        DemandConfigVentilationDomeValueObject config = new DemandConfigVentilationDomeValueObject();
        config.setNaturalVentilationTimeIntervals(Collections.singletonList(
                new DemandTimeIntervalValueObject()
                        .setBeginTime(DemandTimeValueObject.ofAbsoluteTimeByMin())
                        .setEndTime(new DemandTimeValueObject()
                                .setTimeType(DemandTimeTypeEnum.ABSOLUTE)
                                .setAbsoluteTime(LocalTime.of(6, 30)))));

        String json = DemandConfigVentilationDomeValueObjectTest.objectMapper.writeValueAsString(config);
        JsonNode timeIntervalsNode = DemandConfigVentilationDomeValueObjectTest.objectMapper.readTree(json)
                .get("naturalVentilationTimeIntervals");
        Assertions.assertNotNull(timeIntervalsNode);
        Assertions.assertEquals(1, timeIntervalsNode.size());

        DemandConfigVentilationDomeValueObject roundTripConfig = this.deserialize(json);
        Assertions.assertNotNull(roundTripConfig.getNaturalVentilationTimeIntervals());
        Assertions.assertEquals(1, roundTripConfig.getNaturalVentilationTimeIntervals().size());
        Assertions.assertEquals(LocalTime.of(6, 30), roundTripConfig.getNaturalVentilationTimeIntervals()
                .get(0).getEndTime().getAbsoluteTime());
    }

    /**
     * 字段缺失时保持 null, 显式 null 或空数组按既有语义落为非 null 空集合。
     *
     * @since v7.260802.2
     */
    @Test
    void deserializeShouldFollowExistingNullAndEmptySemantics() throws Exception {
        DemandConfigVentilationDomeValueObject missingFieldConfig = this.deserialize("{\"tdbTarget\": 40}");
        Assertions.assertNull(missingFieldConfig.getNaturalVentilationTimeIntervals());

        DemandConfigVentilationDomeValueObject explicitNullConfig = this.deserialize(
                "{\"naturalVentilationTimeIntervals\": null}");
        Assertions.assertNotNull(explicitNullConfig.getNaturalVentilationTimeIntervals());
        Assertions.assertTrue(explicitNullConfig.getNaturalVentilationTimeIntervals().isEmpty());

        DemandConfigVentilationDomeValueObject emptyArrayConfig = this.deserialize(
                "{\"naturalVentilationTimeIntervals\": []}");
        Assertions.assertNotNull(emptyArrayConfig.getNaturalVentilationTimeIntervals());
        Assertions.assertTrue(emptyArrayConfig.getNaturalVentilationTimeIntervals().isEmpty());
    }

    /**
     * 仅配置自然通风需求时间的对象应判定为存在配置。
     *
     * @since v7.260802.2
     */
    @Test
    void anyFieldNotEmptyShouldTreatNaturalVentilationTimeIntervalsAsConfigured() {
        DemandConfigVentilationDomeValueObject emptyConfig = new DemandConfigVentilationDomeValueObject();
        Assertions.assertTrue(emptyConfig.allFieldEmpty());
        Assertions.assertFalse(emptyConfig.anyFieldNotEmpty());

        DemandConfigVentilationDomeValueObject config = new DemandConfigVentilationDomeValueObject();
        config.setNaturalVentilationTimeIntervals(Collections.singletonList(
                new DemandTimeIntervalValueObject()
                        .setBeginTime(DemandTimeValueObject.ofAbsoluteTimeByMin())
                        .setEndTime(DemandTimeValueObject.ofAbsoluteTimeByMin())));
        Assertions.assertTrue(config.anyFieldNotEmpty());
        Assertions.assertFalse(config.allFieldEmpty());
    }

    /**
     * 反序列化穹顶配置。
     *
     * @param json 配置 JSON
     * @return 穹顶配置值对象
     * @since v7.260802.2
     */
    private DemandConfigVentilationDomeValueObject deserialize(String json) throws Exception {
        return DemandConfigVentilationDomeValueObjectTest.objectMapper
                .readValue(json, DemandConfigVentilationDomeValueObject.class);
    }
}
