package com.persagy.apccontrol.basic.model.base;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.bag.HashBag;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Objects;

/**
 * 基础检查参数提供者
 *
 * @author JiYinchuan
 * @since v5.8.0
 */
public interface BaseCheckParamProvider {

    /**
     * 检查并设置参数
     *
     * @since v5.8.0
     */
    default void checkAndSet() {

    }

    /**
     * 检查联合集合是否为 {@code null}
     *
     * @param source  数据源
     * @param sources 数据源集合
     * @param <T>     数据源类型
     * @param <C>     数据源集合类型
     * @return 是否为 {@code null}
     * @since 5.240602.0
     */
    default <T, C extends Collection<T>> boolean checkAssembleCollectionNull(
            @Nullable T source, @Nullable C sources) {
        return ObjectUtils.isEmpty(source) && CollectionUtils.isEmpty(sources);
    }

    /**
     * 检查Pair自身和内部值是否为 {@code null}
     *
     * @param source 数据源
     * @return 是否为 {@code null}
     * @since v6.0.0
     */
    default boolean checkPairAnyNull(@Nullable Pair<?, ?> source) {
        return Objects.nonNull(source) && (Objects.isNull(source.getLeft()) || Objects.isNull(source.getRight()));
    }

    /**
     * 检查元素是否重复
     *
     * @param source 数据源
     * @return 元素是否重复
     * @since 24Q1 T3S1-5.17.0
     */
    default boolean checkRepeat(@Nullable Collection<Object> source) {
        Bag<Object> bag = new HashBag<>(source);
        for (Object bagItem : bag) {
            if (bag.getCount(bagItem) > 1) {
                return true;
            }
        }
        return false;
    }
}
