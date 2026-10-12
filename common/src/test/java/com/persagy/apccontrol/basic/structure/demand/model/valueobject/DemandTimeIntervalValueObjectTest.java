package com.persagy.apccontrol.basic.structure.demand.model.valueobject;

import com.persagy.apccontrol.basic.model.dto.weather.WeatherSunRiseAndSetDTO;
import com.persagy.apccontrol.basic.model.vo.kitchen.KitchenRangeHoodDemandTimeItemVO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTimeTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 需求时间区间值对象(解析与合并)测试
 *
 * @author JiYinchuan
 * @since v7.261001.0
 */
public class DemandTimeIntervalValueObjectTest {

    /**
     * 验证完全相同的时段去重后只保留一条
     *
     * @since v7.261001.0
     */
    @Test
    public void shouldMergeDuplicateIntervals() {
        List<DemandTimeIntervalValueObject> mergedIntervals = DemandTimeIntervalValueObject.merge(
                Arrays.asList(this.absoluteInterval("07:30", "12:00"), this.absoluteInterval("07:30", "12:00")),
                null);

        Assertions.assertEquals(Collections.singletonList("07:30-12:00"), this.format(mergedIntervals));
    }

    /**
     * 验证重叠的时段归并为一条
     *
     * @since v7.261001.0
     */
    @Test
    public void shouldMergeOverlappedIntervals() {
        List<DemandTimeIntervalValueObject> mergedIntervals = DemandTimeIntervalValueObject.merge(
                Arrays.asList(this.absoluteInterval("07:30", "12:00"), this.absoluteInterval("11:00", "13:00")),
                null);

        Assertions.assertEquals(Collections.singletonList("07:30-13:00"), this.format(mergedIntervals));
    }

    /**
     * 验证相接的时段(前一结束时间等于后一开始时间)视为连续归并为一条
     *
     * @since v7.261001.0
     */
    @Test
    public void shouldMergeAdjacentIntervals() {
        List<DemandTimeIntervalValueObject> mergedIntervals = DemandTimeIntervalValueObject.merge(
                Arrays.asList(this.absoluteInterval("07:30", "12:00"), this.absoluteInterval("12:00", "13:00")),
                null);

        Assertions.assertEquals(Collections.singletonList("07:30-13:00"), this.format(mergedIntervals));
    }

    /**
     * 验证被包含的时段归并到外层时段
     *
     * @since v7.261001.0
     */
    @Test
    public void shouldAbsorbContainedInterval() {
        List<DemandTimeIntervalValueObject> mergedIntervals = DemandTimeIntervalValueObject.merge(
                Arrays.asList(this.absoluteInterval("07:30", "12:00"), this.absoluteInterval("09:00", "10:00")),
                null);

        Assertions.assertEquals(Collections.singletonList("07:30-12:00"), this.format(mergedIntervals));
    }

    /**
     * 验证乱序输入按开始时间升序输出, 重叠时段归并, 不连续时段保持独立
     *
     * @since v7.261001.0
     */
    @Test
    public void shouldSortByBeginTimeAndKeepDiscreteIntervals() {
        List<DemandTimeIntervalValueObject> mergedIntervals = DemandTimeIntervalValueObject.merge(
                Arrays.asList(
                        this.absoluteInterval("15:00", "16:00"),
                        this.absoluteInterval("13:00", "14:00"),
                        this.absoluteInterval("07:30", "12:00"),
                        this.absoluteInterval("12:30", "13:30")),
                null);

        Assertions.assertEquals(
                Arrays.asList("07:30-12:00", "12:30-14:00", "15:00-16:00"),
                this.format(mergedIntervals));
    }

    /**
     * 验证跨天区间拆为当天两段(22:00-24:00 与 00:00-06:00)并按开始时间升序输出
     *
     * @since v7.261001.0
     */
    @Test
    public void shouldSplitAcrossDayInterval() {
        DemandTimeIntervalValueObject acrossDayInterval = new DemandTimeIntervalValueObject()
                .setBeginTime(this.absoluteTime("22:00"))
                .setEndTime(this.absoluteTime("06:00"))
                .setAcrossDay(true);

        List<DemandTimeIntervalValueObject> mergedIntervals = DemandTimeIntervalValueObject.merge(
                Collections.singletonList(acrossDayInterval), null);

        Assertions.assertEquals(Arrays.asList("00:00-06:00", "22:00-24:00"), this.format(mergedIntervals));
    }

    /**
     * 验证结束时间为 0 点(当天结束)的时段归并为全天 00:00-24:00
     *
     * @since v7.261001.0
     */
    @Test
    public void shouldMergeToFullDay() {
        List<DemandTimeIntervalValueObject> mergedIntervals = DemandTimeIntervalValueObject.merge(
                Collections.singletonList(this.absoluteInterval("00:00", "00:00")), null);

        Assertions.assertEquals(Collections.singletonList("00:00-24:00"), this.format(mergedIntervals));
    }

    /**
     * 验证日出日落类型时段按当天日出日落时间绝对化后参与合并
     *
     * @since v7.261001.0
     */
    @Test
    public void shouldResolveSunRiseSetIntervalWithWeather() {
        WeatherSunRiseAndSetDTO sunRiseAndSet = new WeatherSunRiseAndSetDTO();
        sunRiseAndSet.setSunRise(LocalDateTime.of(2026, 10, 10, 6, 30));
        sunRiseAndSet.setSunSet(LocalDateTime.of(2026, 10, 10, 18, 30));
        DemandTimeIntervalValueObject sunRiseSetInterval = new DemandTimeIntervalValueObject()
                .setBeginTime(new DemandTimeValueObject().setTimeType(DemandTimeTypeEnum.SUNRISE))
                .setEndTime(new DemandTimeValueObject().setTimeType(DemandTimeTypeEnum.SUNSET));

        List<DemandTimeIntervalValueObject> mergedIntervals = DemandTimeIntervalValueObject.merge(
                Collections.singletonList(sunRiseSetInterval), sunRiseAndSet);

        Assertions.assertEquals(Collections.singletonList("06:30-18:30"), this.format(mergedIntervals));
    }

    /**
     * 验证日出日落数据缺失时对应时段被跳过
     *
     * @since v7.261001.0
     */
    @Test
    public void shouldSkipSunRiseSetIntervalWhenWeatherMissing() {
        DemandTimeIntervalValueObject sunRiseSetInterval = new DemandTimeIntervalValueObject()
                .setBeginTime(new DemandTimeValueObject().setTimeType(DemandTimeTypeEnum.SUNRISE))
                .setEndTime(this.absoluteTime("12:00"));

        List<DemandTimeIntervalValueObject> mergedIntervals = DemandTimeIntervalValueObject.merge(
                Collections.singletonList(sunRiseSetInterval), null);

        Assertions.assertTrue(mergedIntervals.isEmpty());
    }

    /**
     * 验证空元素、不完整区间与结束早于开始的无效区间均被跳过
     *
     * @since v7.261001.0
     */
    @Test
    public void shouldSkipInvalidAndEmptyIntervals() {
        DemandTimeIntervalValueObject incompleteInterval = new DemandTimeIntervalValueObject()
                .setBeginTime(this.absoluteTime("07:30"));
        DemandTimeIntervalValueObject reversedInterval = this.absoluteInterval("12:00", "07:30");

        List<DemandTimeIntervalValueObject> mergedIntervals = DemandTimeIntervalValueObject.merge(
                Arrays.asList(null, incompleteInterval, reversedInterval), null);

        Assertions.assertTrue(mergedIntervals.isEmpty());
    }

    /**
     * 验证空入参与空集合返回空结果
     *
     * @since v7.261001.0
     */
    @Test
    public void shouldReturnEmptyWhenIntervalsEmpty() {
        Assertions.assertTrue(DemandTimeIntervalValueObject.merge(null, null).isEmpty());
        Assertions.assertTrue(DemandTimeIntervalValueObject.merge(Collections.emptyList(), null).isEmpty());
    }

    /**
     * 验证时间格式化, 结束点 00:00 输出 24:00
     *
     * @since v7.261001.0
     */
    @Test
    public void shouldFormatTime() {
        DemandTimeValueObject midnight = DemandTimeValueObject.ofAbsoluteTime(LocalTime.MIN);
        DemandTimeValueObject sevenThirty = DemandTimeValueObject.ofAbsoluteTime(LocalTime.of(7, 30));
        DemandTimeIntervalValueObject morningInterval = new DemandTimeIntervalValueObject()
                .setBeginTime(midnight)
                .setEndTime(sevenThirty);
        DemandTimeIntervalValueObject toMidnightInterval = new DemandTimeIntervalValueObject()
                .setBeginTime(sevenThirty)
                .setEndTime(midnight);

        Assertions.assertEquals("00:00", KitchenRangeHoodDemandTimeItemVO.of(morningInterval).getBeginTime());
        Assertions.assertEquals("07:30", KitchenRangeHoodDemandTimeItemVO.of(morningInterval).getEndTime());
        Assertions.assertEquals("24:00", KitchenRangeHoodDemandTimeItemVO.of(toMidnightInterval).getEndTime());
    }

    /**
     * 构造绝对时间需求时间区间
     *
     * @param beginTime 开始时间, 格式 HH:mm
     * @param endTime   结束时间, 格式 HH:mm
     * @return 需求时间区间
     * @since v7.261001.0
     */
    private DemandTimeIntervalValueObject absoluteInterval(String beginTime, String endTime) {
        return new DemandTimeIntervalValueObject()
                .setBeginTime(this.absoluteTime(beginTime))
                .setEndTime(this.absoluteTime(endTime));
    }

    /**
     * 构造绝对时间需求时间
     *
     * @param time 时间, 格式 HH:mm
     * @return 绝对时间需求时间
     * @since v7.261001.0
     */
    private DemandTimeValueObject absoluteTime(String time) {
        return new DemandTimeValueObject()
                .setTimeType(DemandTimeTypeEnum.ABSOLUTE)
                .setAbsoluteTime(LocalTime.parse(time));
    }

    /**
     * 将合并区间格式化为 begin-end 字符串列表, 便于断言
     *
     * @param mergedIntervals 合并后的绝对时间区间列表
     * @return 格式化结果
     * @since v7.261001.0
     */
    private List<String> format(List<DemandTimeIntervalValueObject> mergedIntervals) {
        return mergedIntervals.stream()
                .map(interval -> {
                    KitchenRangeHoodDemandTimeItemVO item = KitchenRangeHoodDemandTimeItemVO.of(interval);
                    return item.getBeginTime() + "-" + item.getEndTime();
                })
                .collect(Collectors.toList());
    }
}
