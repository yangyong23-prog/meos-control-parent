package com.persagy.apccontrol.basic.config.dao.partition;

import com.persagy.validate.utility.enumeration.DateTimeTypeEnum;
import com.persagy.validate.utility.executor.DateTimeRangeExecutor;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 按日期分表策略
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
public class DateTimePartitionTableStrategy extends AbstractPartitionTableStrategy {

    /**
     * 分表时间
     */
    private final String dateTime;

    /**
     * 构造器
     *
     * @param dateTime 分表时间
     * @since v6.0.0
     */
    private DateTimePartitionTableStrategy(String dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String getPartitionTableSuffix() {
        return dateTime;
    }

    /**
     * 获取当前实例
     *
     * @param dateTime 分表时间
     * @return 当前实例
     * @since v6.0.0
     */
    public static DateTimePartitionTableStrategy instance(@NonNull String dateTime) {
        return new DateTimePartitionTableStrategy(dateTime);
    }

    /**
     * 获取当前实例
     *
     * @param formatter 格式化时间
     * @param dateTime  分表时间
     * @return 当前实例
     * @since v6.0.0
     */
    public static DateTimePartitionTableStrategy instance(
            @NonNull DateTimeFormatter formatter, @NonNull LocalDateTime dateTime) {
        return DateTimePartitionTableStrategy.instance(formatter.format(dateTime));
    }

    /**
     * 获取当前实例
     *
     * @param formatter 格式化时间
     * @param dateTimes 分表时间
     * @return 当前实例
     * @since v6.0.0
     */
    public static Map<String, DateTimePartitionTableStrategy> instance(
            @NonNull DateTimeFormatter formatter, @NonNull Collection<LocalDateTime> dateTimes) {
        return dateTimes.stream().map(formatter::format).collect(Collectors.toMap(
                Function.identity(), DateTimePartitionTableStrategy::instance, (o1, o2) -> o1));
    }

    /**
     * 获取当前实例
     *
     * @param formatter 格式化时间
     * @param dateTimes 分表时间
     * @param callback  回调函数
     * @return 当前实例
     * @since v6.0.0
     */
    public static <T> List<T> instance(
            @NonNull DateTimeFormatter formatter, @NonNull Collection<LocalDateTime> dateTimes,
            @NonNull BiFunction<String, DateTimePartitionTableStrategy, T> callback) {
        return instance(formatter, dateTimes).entrySet().stream().map(item ->
                callback.apply(item.getKey(), item.getValue())).collect(Collectors.toList());
    }

    /**
     * 获取当前实例
     *
     * @param formatter 格式化时间
     * @param dateTimes 分表时间
     * @param callback  回调函数
     * @since v6.0.0
     */
    public static void instance(
            @NonNull DateTimeFormatter formatter, @NonNull Collection<LocalDateTime> dateTimes,
            @NonNull BiConsumer<String, DateTimePartitionTableStrategy> callback) {
        instance(formatter, dateTimes).forEach(callback);
    }

    /**
     * 获取当前实例
     *
     * @param formatter     格式化时间
     * @param beginDateTime 开始时间
     * @param endDateTime   结束时间
     * @return 当前实例
     * @since v6.0.0
     */
    public static Map<String, DateTimePartitionTableStrategy> instance(
            @NonNull DateTimeTypeEnum dateTimeType, @NonNull DateTimeFormatter formatter,
            @NonNull LocalDateTime beginDateTime, @NonNull LocalDateTime endDateTime) {
        Map<String, DateTimePartitionTableStrategy> result = new HashMap<>(16);
        DateTimeRangeExecutor.generateDateTime(dateTimeType, beginDateTime, endDateTime, eachTime -> {
            String partitionTime = formatter.format(eachTime);
            if (!result.containsKey(partitionTime)) {
                result.put(partitionTime, DateTimePartitionTableStrategy.instance(partitionTime));
            }
        });
        return result;
    }

    /**
     * 获取当前实例
     *
     * @param formatter     格式化时间
     * @param beginDateTime 开始时间
     * @param endDateTime   结束时间
     * @param callback      回调函数
     * @return 当前实例
     * @since v6.0.0
     */
    public static <T> List<T> instance(
            @NonNull DateTimeTypeEnum dateTimeType, @NonNull DateTimeFormatter formatter,
            @NonNull LocalDateTime beginDateTime, @NonNull LocalDateTime endDateTime,
            @NonNull BiFunction<String, DateTimePartitionTableStrategy, T> callback) {
        return instance(dateTimeType, formatter, beginDateTime, endDateTime).entrySet().stream().map(item ->
                callback.apply(item.getKey(), item.getValue())).collect(Collectors.toList());
    }

    /**
     * 获取当前实例
     *
     * @param formatter     格式化时间
     * @param beginDateTime 开始时间
     * @param endDateTime   结束时间
     * @param callback      回调函数
     * @since v6.0.0
     */
    public static void instance(
            @NonNull DateTimeTypeEnum dateTimeType, @NonNull DateTimeFormatter formatter,
            @NonNull LocalDateTime beginDateTime, @NonNull LocalDateTime endDateTime,
            @NonNull BiConsumer<String, DateTimePartitionTableStrategy> callback) {
        instance(dateTimeType, formatter, beginDateTime, endDateTime).forEach(callback);
    }
}
