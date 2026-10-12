package com.persagy.apccontrol.basic.utils.template;

import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SPE工具类
 *
 * @author JiYinchuan
 * @since 7.250501.0
 */
public class SpelUtils {

    /**
     * SPEL表达式解析器
     */
    private static final SpelExpressionParser SPEL_EXPRESSION_PARSER = new SpelExpressionParser();

    /**
     * 获取SPEL表达式解析器
     *
     * @return SPEL表达式解析器
     * @since 7.250501.0
     */
    public static SpelExpressionParser getSpelExpressionParser() {
        return SPEL_EXPRESSION_PARSER;
    }

    /**
     * 获取默认标准评估上下文
     *
     * @param rootObject 根对象
     * @return 默认标准评估上下文
     * @since 7.250501.0
     */
    public static StandardEvaluationContext getDefaultStandardEvaluationContext(@Nullable Object rootObject) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setRootObject(rootObject);
        // 集合并集
        registerFunction(context, "unionCollection", Collection[].class);
        // 集合交集
        registerFunction(context, "intersectionCollection", Collection[].class);
        // 集合差集
        registerFunction(context, "subtractCollection", Collection[].class);
        // 集合元素展开
        registerFunction(context, "flattenSet", Collection.class);
        return context;
    }

    /**
     * 注册方法
     *
     * @param context    上下午
     * @param methodName 方法名称
     * @since 7.250501.0
     */
    @SneakyThrows
    private static void registerFunction(
            @NonNull StandardEvaluationContext context,
            @NonNull String methodName,
            @Nullable Class<?>... parameterTypes) {
        context.registerFunction(methodName, SpelUtils.class.getDeclaredMethod(methodName, parameterTypes));
    }

    /**
     * 集合展开为Set
     *
     * @param collection 集合
     * @param <T>        集合中元素类型
     * @return Set
     * @since 7.250601.0
     */
    @SuppressWarnings("unused")
    private static <T> @Nullable Set<T> flattenSet(
            @Nullable Collection<@Nullable Collection<T>> collection) {
        if (Objects.isNull(collection)) {
            return null;
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * 方法-集合交集
     *
     * @param collections 集合
     * @param <T>         集合中元素类型
     * @return 交集后的集合
     * @see #getDefaultStandardEvaluationContext(Object)
     * @since 7.250601.0
     */
    @SuppressWarnings({"unchecked", "unused"})
    private static <T> @Nullable Collection<T> intersectionCollection(
            @Nullable Collection<@Nullable T> @Nullable ... collections) {
        if (Objects.isNull(collections) || collections.length == 0) {
            return null;
        }
        Collection<T> result = collections[0];
        // 入参中集合第一个为 null 时, 返回 null
        if (Objects.isNull(result)) {
            return null;
        }
        for (int i = 1; i < collections.length; i++) {
            Collection<@Nullable T> collection = collections[i];
            // 入参中集合任何一个为 null 时, 返回 null
            if (Objects.isNull(collection)) {
                return null;
            }
            result = CollectionUtils.intersection(result, collection);
        }
        return result;
    }

    /**
     * 方法-集合并集
     *
     * @param collections 集合
     * @param <T>         集合中元素类型
     * @return 合并后的集合
     * @see #getDefaultStandardEvaluationContext(Object)
     * @since 7.250501.0
     */
    @SuppressWarnings({"unchecked", "unused"})
    private static <T> @Nullable Collection<T> unionCollection(
            @Nullable Collection<@Nullable T> @Nullable ... collections) {
        if (Objects.isNull(collections) || collections.length == 0) {
            return null;
        }
        Collection<T> result = new ArrayList<>();
        int emptySize = 0;
        for (Collection<T> collection : collections) {
            if (CollectionUtils.isEmpty(collection)) {
                emptySize++;
                continue;
            }
            result.addAll(collection);
        }
        if (emptySize == collections.length) {
            return null;
        }
        return result;
    }

    /**
     * 方法-集合差集
     *
     * @param collections 集合
     * @param <T>         集合中元素类型
     * @return 合并后的集合
     * @see #getDefaultStandardEvaluationContext(Object)
     * @since 7.250701.0
     */
    @SuppressWarnings({"unchecked", "unused"})
    private static <T> @Nullable Collection<T> subtractCollection(
            @Nullable Collection<@Nullable T> @Nullable ... collections) {
        if (Objects.isNull(collections) || collections.length == 0) {
            return null;
        }
        Collection<T> result = collections[0];
        // 入参中集合第一个为 null 时, 返回 null
        if (Objects.isNull(result)) {
            return null;
        }
        for (int i = 1; i < collections.length; i++) {
            Collection<@Nullable T> collection = collections[i];
            if (Objects.isNull(collection)) {
                continue;
            }
            result = CollectionUtils.subtract(result, collection);
        }
        // 两个集合相减后结果为空时, 返回 null
        if (CollectionUtils.isEmpty(result)) {
            return null;
        }
        return result;
    }
}
