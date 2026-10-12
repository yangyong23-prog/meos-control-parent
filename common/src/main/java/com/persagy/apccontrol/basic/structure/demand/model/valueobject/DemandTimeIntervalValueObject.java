package com.persagy.apccontrol.basic.structure.demand.model.valueobject;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.model.dto.weather.WeatherSunRiseAndSetDTO;
import com.persagy.validate.utility.enumeration.TimeTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 需求时间区间值对象
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
@Accessors(chain = true)
public class DemandTimeIntervalValueObject implements ValueObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandTimeIntervalValueObject.class);
    private static final String LOG_TAG = "需求时间区间值对象";

    /**
     * 开始时间
     */
    private DemandTimeValueObject beginTime;

    /**
     * 结束时间
     */
    private DemandTimeValueObject endTime;

    /**
     * 是否跨天, 默认不跨天
     */
    private Boolean acrossDay;

    /**
     * 根据日期解析时间区间
     *
     * @param timeType      时间类型, 用于Range中的处理结束时间
     * @param sunRiseAndSet 日出日落时间, 当时间区间包含日出或日落时间时不能为空
     * @return 解析后的时间区间
     * @since 5.240902.0
     */
    public List<Range<LocalTime>> analyzeTimeInterval(
            @NonNull TimeTypeEnum timeType,
            @Nullable WeatherSunRiseAndSetDTO sunRiseAndSet) {
        if (checkTimeIntervalNotValid(true, sunRiseAndSet)) {
            return null;
        }
        TemporalUnit temporalUnit = timeType.convertTemporalUnit();
        LocalTime absoluteBeginTime = beginTime.analyzeAbsoluteTime(sunRiseAndSet);
        LocalTime absoluteEndTime = endTime.analyzeAbsoluteTime(sunRiseAndSet);
        if (BooleanUtils.isTrue(acrossDay)) {
            return Lists.newArrayList(
                    Range.between(absoluteBeginTime, LocalTime.MIN.minus(1, temporalUnit)),
                    Range.between(LocalTime.MIN, absoluteEndTime.minus(1, temporalUnit))
            );
        }
        // 当跨天或结束时间为0点时, 结束时间为第二天的时间
        if (absoluteEndTime.equals(LocalTime.MIN)) {
            absoluteEndTime = LocalTime.MIN.minus(1, temporalUnit);
        }
        // 当不跨天时, 结束时间小于开始时间为无效时间
        if (absoluteEndTime.isBefore(absoluteBeginTime)) {
            LogExecutor.project().warn(LOGGER, LOG_TAG, "结束时间小于开始时间", Arrays.asList(
                    Pair.of("beginTime", beginTime),
                    Pair.of("endTime", endTime)
            ));
            return Lists.newArrayList();
        }
        return Lists.newArrayList(Range.between(absoluteBeginTime, absoluteEndTime));
    }

    /**
     * 是否为日出日落时间类型区间
     *
     * @return 开始或结束时间为日出/日落时间时返回 {@literal true}
     * @since v7.261001.0
     */
    public boolean isSunRiseAndSetTime() {
        return (Objects.nonNull(beginTime) && beginTime.isSunRiseAndSetTimeType())
                || (Objects.nonNull(endTime) && endTime.isSunRiseAndSetTimeType());
    }

    /**
     * 解析并转换为绝对时间区间(开始/结束均为 ABSOLUTE 类型的 {@link DemandTimeValueObject})
     *
     * <p>analyzeTimeInterval 返回的区间端点存在两种历史语义, 统一还原为显示区间 {@code [begin, end)}:
     * 普通区间端点原样返回, 结束点即显示结束时间; 结束于当天末(结束为 0 点或跨天拆分第一段)与跨天拆分的第二段,
     * 结束点被减 1 分钟, 需要还原(结束点 00:00 表示 24:00)。</p>
     *
     * @param sunRiseAndSet 日出日落时间, 区间包含日出日落时间类型时不能为空
     * @return 绝对时间区间列表(跨天拆段时为两条), 永不为 {@literal null}
     * @since v7.261001.0
     */
    public @NonNull List<DemandTimeIntervalValueObject> toAbsoluteIntervals(
            @Nullable WeatherSunRiseAndSetDTO sunRiseAndSet) {
        List<Range<LocalTime>> ranges;
        try {
            ranges = this.analyzeTimeInterval(TimeTypeEnum.MINUTE, sunRiseAndSet);
        } catch (RuntimeException e) {
            // 单条脏数据(如绝对时间缺失)只跳过该条, 不中断整体合并流程
            LogExecutor.project().warn(LOGGER, LOG_TAG, "需求时间区间解析失败, 已跳过", e,
                    Pair.of("interval", this));
            return new ArrayList<>();
        }
        // 无效区间(区间不完整/日出日落数据缺失/结束早于开始)解析结果为空, 同样跳过
        if (CollectionUtils.isEmpty(ranges)) {
            return new ArrayList<>();
        }
        List<DemandTimeIntervalValueObject> absoluteIntervals = new ArrayList<>(ranges.size());
        for (int index = 0; index < ranges.size(); index++) {
            Range<LocalTime> range = ranges.get(index);
            LocalTime endTime = range.getMaximum();
            // analyzeTimeInterval 的结束点为含端点语义, 统一还原为显示区间 [begin, end):
            // 结束点到达当天末(23:59)时还原为 00:00(即 24:00); 跨天拆分第二段的结束点为 end-1 分钟, 补回 1 分钟
            if (!endTime.isBefore(LocalTime.of(23, 59))) {
                endTime = LocalTime.MIN;
            } else if (ranges.size() == 2 && index == 1) {
                endTime = endTime.plusMinutes(1);
            }
            absoluteIntervals.add(new DemandTimeIntervalValueObject()
                    .setBeginTime(DemandTimeValueObject.ofAbsoluteTime(range.getMinimum()))
                    .setEndTime(DemandTimeValueObject.ofAbsoluteTime(endTime)));
        }
        return absoluteIntervals;
    }

    /**
     * 检查时间区间是否无效
     *
     * @param printLog      是否打印日志
     * @param sunRiseAndSet 日出日落时间
     * @return 时间区间是否无效
     * @since 5.240902.0
     */
    public boolean checkTimeIntervalNotValid(boolean printLog, @Nullable WeatherSunRiseAndSetDTO sunRiseAndSet) {
        if (ObjectUtils.anyNull(beginTime, endTime)) {
            if (printLog) {
                LogExecutor.project().warn(LOGGER, LOG_TAG, "时间区间不完整", Arrays.asList(
                        Pair.of("beginTime", beginTime),
                        Pair.of("endTime", endTime)
                ));
            }
            return true;
        }
        if ((beginTime.isSunRiseAndSetTimeType() || endTime.isSunRiseAndSetTimeType()) && Objects.isNull(sunRiseAndSet)) {
            if (printLog) {
                LogExecutor.project().warn(LOGGER, LOG_TAG, "时间区间存在日出日落时间类型, 且日出日落时间为空", Arrays.asList(
                        Pair.of("beginTime", beginTime),
                        Pair.of("endTime", endTime)
                ));
            }
            return true;
        }
        return false;
    }

    /**
     * 初始化完整时间区间值对象
     *
     * @return 完整时间区间值对象
     * @since 5.240602.0
     */
    public static DemandTimeIntervalValueObject initFullDayData() {
        DemandTimeIntervalValueObject intervalOneDay = new DemandTimeIntervalValueObject();
        intervalOneDay.setBeginTime(DemandTimeValueObject.ofAbsoluteTimeByMin());
        intervalOneDay.setEndTime(DemandTimeValueObject.ofAbsoluteTimeByMin());
        return intervalOneDay;
    }

    /**
     * 解析并合并需求时间区间集合(产物为绝对时间区间, 结束点 00:00 表示 24:00)
     *
     * @param intervals     需求时间区间集合, 可为 {@literal null}
     * @param sunRiseAndSet 日出日落时间, 区间包含日出日落时间类型时不能为空
     * @return 合并后的绝对时间区间列表, 按开始时间升序, 永不为 {@literal null}
     * @since v7.261001.0
     */
    public static @NonNull List<DemandTimeIntervalValueObject> merge(
            @Nullable Collection<DemandTimeIntervalValueObject> intervals,
            @Nullable WeatherSunRiseAndSetDTO sunRiseAndSet) {
        List<DemandTimeIntervalValueObject> mergedIntervals = new ArrayList<>();
        if (CollectionUtils.isEmpty(intervals)) {
            return mergedIntervals;
        }
        for (DemandTimeIntervalValueObject interval : intervals) {
            if (Objects.isNull(interval)) {
                continue;
            }
            mergedIntervals.addAll(interval.toAbsoluteIntervals(sunRiseAndSet));
        }
        return mergeIntervals(mergedIntervals);
    }

    /**
     * 合并绝对时间区间集合(去重与重叠归并, 相接视为连续)
     *
     * @param intervals 绝对时间区间集合, 可为 {@literal null}
     * @return 合并后的绝对时间区间列表, 按开始时间升序, 永不为 {@literal null}
     * @since v7.261001.0
     */
    public static @NonNull List<DemandTimeIntervalValueObject> mergeIntervals(
            @Nullable Collection<DemandTimeIntervalValueObject> intervals) {
        if (CollectionUtils.isEmpty(intervals)) {
            return new ArrayList<>();
        }
        // 按开始时间升序排序后单遍扫描: 区间开始时间不晚于当前合并段结束点(含相接)时归并
        List<DemandTimeIntervalValueObject> sortedIntervals = intervals.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(interval -> interval.getBeginTime().getAbsoluteTime()))
                .collect(Collectors.toList());
        List<DemandTimeIntervalValueObject> mergedIntervals = new ArrayList<>();
        DemandTimeIntervalValueObject current = null;
        for (DemandTimeIntervalValueObject interval : sortedIntervals) {
            if (Objects.isNull(current)) {
                current = copyAbsoluteInterval(interval);
                continue;
            }
            LocalTime intervalBeginTime = interval.getBeginTime().getAbsoluteTime();
            LocalTime intervalEndTime = interval.getEndTime().getAbsoluteTime();
            LocalTime currentEndTime = current.getEndTime().getAbsoluteTime();
            // 与当前合并段相接或重叠时并入, 否则收束当前段并另起新段
            if (isMergeable(intervalBeginTime, currentEndTime)) {
                if (isEndLater(intervalEndTime, currentEndTime)) {
                    current.setEndTime(DemandTimeValueObject.ofAbsoluteTime(intervalEndTime));
                }
            } else {
                mergedIntervals.add(current);
                current = copyAbsoluteInterval(interval);
            }
        }
        if (Objects.nonNull(current)) {
            mergedIntervals.add(current);
        }
        return mergedIntervals;
    }

    /**
     * 复制绝对时间区间(避免归并过程修改调用方传入的对象)
     *
     * @param interval 绝对时间区间
     * @return 绝对时间区间副本
     * @since v7.261001.0
     */
    private static DemandTimeIntervalValueObject copyAbsoluteInterval(
            @NonNull DemandTimeIntervalValueObject interval) {
        return new DemandTimeIntervalValueObject()
                .setBeginTime(DemandTimeValueObject.ofAbsoluteTime(interval.getBeginTime().getAbsoluteTime()))
                .setEndTime(DemandTimeValueObject.ofAbsoluteTime(interval.getEndTime().getAbsoluteTime()));
    }

    /**
     * 判断区间开始时间是否可与当前合并段归并(相接或重叠)
     *
     * <p>开始时间不晚于当前结束点(含相接)即可归并; 结束点 00:00 表示 24:00(当天结束), 视为当天最晚时间。</p>
     *
     * @param beginTime      待归并区间的开始时间
     * @param currentEndTime 当前合并段的结束时间
     * @return 是否可归并
     * @since v7.261001.0
     */
    private static boolean isMergeable(@NonNull LocalTime beginTime, @NonNull LocalTime currentEndTime) {
        if (LocalTime.MIN.equals(currentEndTime)) {
            // 当前段结束于 24:00, 任何开始时间均可归并
            return true;
        }
        return !beginTime.isAfter(currentEndTime);
    }

    /**
     * 判断结束时间是否晚于当前合并段的结束时间
     *
     * <p>结束点 00:00 表示 24:00(当天结束), 视为当天最晚时间。</p>
     *
     * @param endTime        结束时间
     * @param currentEndTime 当前合并段的结束时间
     * @return 是否更晚
     * @since v7.261001.0
     */
    private static boolean isEndLater(@NonNull LocalTime endTime, @NonNull LocalTime currentEndTime) {
        if (LocalTime.MIN.equals(currentEndTime)) {
            // 当前段结束于 24:00, 恒为最晚
            return false;
        }
        if (LocalTime.MIN.equals(endTime)) {
            // 结束点 00:00 表示 24:00, 晚于当前结束时间
            return true;
        }
        return endTime.isAfter(currentEndTime);
    }
}
