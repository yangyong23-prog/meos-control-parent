package com.persagy.apccontrol.basic.utils.range;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Range;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 时间范围工具类
 *
 * <ul>
 *     <li><b>isOverlapped</b> - 时间范围是否有重叠</li>
 *     <li><b>instance</b> - 创建时间范围实例</li>
 *     <li><b>instanceByString</b> - 根据字符串创建时间范围实例</li>
 *     <li><b>instanceByArray</b> - 根据数组创建时间范围实例</li>
 * </ul>
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
public class RangeUtils {

    public static final String DEFAULT_RANGE_SPLIT_REGEX = ",";

    private static ObjectMapper objectMapper;

    /**
     * 判断时间范围是否有重叠
     *
     * @param source 数据源
     * @param <T>    时间类型
     * @return 时间范围是否有重叠
     * @since v5.7.0
     */
    public static <T extends Comparable<T>> boolean isOverlapped(@Nullable Collection<Range<T>> source) {
        if (CollectionUtils.isEmpty(source)) {
            return false;
        }
        Iterator<Range<T>> iterator = source.iterator();
        while (iterator.hasNext()) {
            Range<T> range = iterator.next();
            if (!iterator.hasNext()) {
                break;
            }
            Range<T> nextRange = iterator.next();
            if (range.isOverlappedBy(nextRange)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断时间范围是否有重叠
     *
     * @param source 数据源
     * @param target 目标源
     * @param <T>    时间类型
     * @return 时间范围是否有重叠
     * @since v5.7.0
     */
    public static <T extends Comparable<T>> boolean isOverlapped(
            @Nullable Collection<Range<T>> source, @Nullable Collection<Range<T>> target) {
        if (CollectionUtils.isEmpty(source)) {
            return false;
        }
        if (isOverlapped(source)) {
            return true;
        }
        if (CollectionUtils.isEmpty(target)) {
            return false;
        }
        if (isOverlapped(target)) {
            return true;
        }
        for (Range<T> sourceItem : source) {
            for (Range<T> targetItem : target) {
                if (sourceItem.isOverlappedBy(targetItem)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 创建范围对象实例
     *
     * @param minimum 最小值
     * @param maximum 最大值
     * @param parser  解析器
     * @param <T>     对象类型
     * @return 范围对象
     * @since v5.7.0
     */
    public static <T extends Comparable<T>> Range<T> instance(
            @NonNull String minimum, @NonNull String maximum,
            @NonNull Function<String, T> parser) {
        return Range.between(parser.apply(minimum), parser.apply(maximum));
    }

    /**
     * 根据字符串创建范围对象实例
     *
     * @param source 数据源
     * @param parser 解析器
     * @param <T>    对象类型
     * @return 范围对象
     * @since v5.7.0
     */
    public static <T extends Comparable<T>> Range<T> instance(
            @NonNull String source, @NonNull Function<String, T> parser) {
        return instanceByString(source, DEFAULT_RANGE_SPLIT_REGEX, parser);
    }

    /**
     * 根据字符串创建范围对象实例
     *
     * @param source 数据源
     * @param regex  定界正则表达式
     * @param parser 解析器
     * @param <T>    对象类型
     * @return 范围对象
     * @since v5.7.0
     */
    public static <T extends Comparable<T>> Range<T> instanceByString(
            @NonNull String source, @NonNull String regex,
            @NonNull Function<String, T> parser) {
        String[] split = source.split(regex);
        return Range.between(parser.apply(split[0]), parser.apply(split[1]));
    }

    /**
     * 创建范围对象实例
     *
     * @param minimum        最小值
     * @param maximum        最大值
     * @param parser         解析器
     * @param resultInstance 结果实例
     * @param <T>            对象类型
     * @param <R>            结果类型
     * @return 范围对象
     * @since v5.7.0
     */
    public static <T extends Comparable<T>, R extends Collection<Range<T>>> R instanceByArray(
            @Nullable List<String> minimum, @Nullable List<String> maximum,
            @NonNull Function<String, T> parser, @NonNull Supplier<R> resultInstance) {
        if (Objects.isNull(minimum) || Objects.isNull(maximum)) {
            return null;
        }
        if (minimum.isEmpty() || maximum.isEmpty()) {
            return resultInstance.get();
        }
        if (minimum.size() != maximum.size()) {
            throw new IllegalArgumentException("The size of minimum and maximum must be the same.");
        }
        R result = resultInstance.get();
        for (int i = 0; i < minimum.size(); i++) {
            result.add(instance(minimum.get(i), maximum.get(i), parser));
        }
        return result;
    }

    /**
     * 根据字符串数组创建范围对象实例
     *
     * @param source         数据源
     * @param parser         解析器
     * @param resultInstance 结果实例
     * @param <T>            对象类型
     * @param <R>            结果类型
     * @return 范围对象
     * @since v5.7.0
     */
    public static <T extends Comparable<T>, R extends Collection<Range<T>>> R instanceByArray(
            @Nullable Collection<String> source, @NonNull Function<String, T> parser,
            @NonNull Supplier<R> resultInstance) {
        return instanceByArray(source, DEFAULT_RANGE_SPLIT_REGEX, parser, resultInstance);
    }

    /**
     * 根据字符串数组创建范围对象实例
     *
     * @param source         数据源
     * @param regex          定界正则表达式
     * @param parser         解析器
     * @param resultInstance 结果实例
     * @param <T>            对象类型
     * @param <R>            结果类型
     * @return 范围对象
     * @since v5.7.0
     */
    public static <T extends Comparable<T>, R extends Collection<Range<T>>> R instanceByArray(
            @Nullable Collection<String> source, @NonNull String regex,
            @NonNull Function<String, T> parser, @NonNull Supplier<R> resultInstance) {
        if (Objects.isNull(source)) {
            return null;
        }
        R result = resultInstance.get();
        if (CollectionUtils.isEmpty(source)) {
            return result;
        }
        for (String sourceItem : source) {
            result.add(instanceByString(sourceItem, regex, parser));
        }
        return result;
    }

    /**
     * 根据Map创建范围对象实例
     *
     * @param source         数据源
     * @param parser         解析器
     * @param resultInstance 结果实例
     * @param <T>            对象类型
     * @param <R>            结果类型
     * @return 范围对象
     * @since 5.241002.0
     */
    public static <T extends Comparable<T>, R extends Collection<Range<T>>> R instanceByMap(
            @Nullable Collection<Map<String, Object>> source, @NonNull Function<String, T> parser,
            @NonNull Supplier<R> resultInstance) {
        return instanceByMap(source, DEFAULT_RANGE_SPLIT_REGEX, parser, resultInstance);
    }

    /**
     * 根据Map创建范围对象实例
     *
     * @param source         数据源
     * @param regex          定界正则表达式
     * @param parser         解析器
     * @param resultInstance 结果实例
     * @param <T>            对象类型
     * @param <R>            结果类型
     * @return 范围对象
     * @since 5.241002.0
     */
    @SneakyThrows
    public static <T extends Comparable<T>, R extends Collection<Range<T>>> R instanceByMap(
            @Nullable Collection<Map<String, Object>> source, @NonNull String regex,
            @NonNull Function<String, T> parser, @NonNull Supplier<R> resultInstance) {
        if (Objects.isNull(source)) {
            return null;
        }
        R result = resultInstance.get();
        if (CollectionUtils.isEmpty(source)) {
            return result;
        }
        for (Map<String, Object> sourceItem : source) {
            if (MapUtils.isEmpty(sourceItem)) {
                continue;
            }
            String valueAsString = getObjectMapper().writeValueAsString(sourceItem);
            result.add(instanceByString(valueAsString, regex, parser));
        }
        return result;
    }

    /**
     * 输出字符串
     *
     * @param source 数据源
     * @param parser 解析器
     * @param <T>    对象类型
     * @return 字符串
     * @since v5.7.0
     */
    public static <T extends Comparable<T>> String toString(
            @Nullable Range<T> source, @NonNull Function<T, String> parser) {
        return toString(source, DEFAULT_RANGE_SPLIT_REGEX, parser);
    }

    /**
     * 输出字符串
     *
     * @param source 数据源
     * @param regex  定界正则表达式
     * @param parser 解析器
     * @param <T>    对象类型
     * @return 字符串
     * @since v5.7.0
     */
    public static <T extends Comparable<T>> String toString(
            @Nullable Range<T> source, @NonNull String regex, @NonNull Function<T, String> parser) {
        if (Objects.isNull(source)) {
            return null;
        }
        return String.join(regex, parser.apply(source.getMinimum()), parser.apply(source.getMaximum()));
    }

    /**
     * 输出字符串
     *
     * @param source         数据源
     * @param parser         解析器
     * @param resultInstance 结果实例
     * @param <T>            对象类型
     * @param <R>            结果类型
     * @return 字符串
     * @since v5.7.0
     */
    public static <T extends Comparable<T>, R extends Collection<String>> R toString(
            @Nullable Collection<Range<T>> source, @NonNull Function<T, String> parser,
            @NonNull Supplier<R> resultInstance) {
        return toString(source, DEFAULT_RANGE_SPLIT_REGEX, parser, resultInstance);
    }

    /**
     * 输出字符串
     *
     * @param source         数据源
     * @param parser         解析器
     * @param resultInstance 结果实例
     * @param <T>            对象类型
     * @param <R>            结果类型
     * @return 字符串
     * @since v5.7.0
     */
    public static <T extends Comparable<T>, R extends Collection<String>> R toString(
            @Nullable Collection<Range<T>> source, @NonNull String regex,
            @NonNull Function<T, String> parser, @NonNull Supplier<R> resultInstance) {
        if (Objects.isNull(source)) {
            return null;
        }
        R result = resultInstance.get();
        for (Range<T> sourceItem : source) {
            result.add(toString(sourceItem, regex, parser));
        }
        return result;
    }

    /**
     * 获取ObjectMapper实例
     *
     * @return ObjectMapper实例
     * @since 5.241002.0
     */
    private static ObjectMapper getObjectMapper() {
        return Optional.ofNullable(objectMapper).orElseGet(() -> SpringUtil.getBean(ObjectMapper.class));
    }
}
