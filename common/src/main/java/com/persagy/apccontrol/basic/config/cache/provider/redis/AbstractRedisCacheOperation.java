package com.persagy.apccontrol.basic.config.cache.provider.redis;

import com.persagy.apccontrol.basic.config.cache.provider.AbstractCacheOperation;
import org.springframework.lang.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 缓存操作抽象类
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
public abstract class AbstractRedisCacheOperation<T> extends AbstractCacheOperation {

    /**
     * 操作委托类
     */
    private final Supplier<T> opsDelegate;

    /**
     * 构造器
     *
     * @param opsDelegate 操作委托类
     * @since v5.7.0
     */
    protected AbstractRedisCacheOperation(Supplier<T> opsDelegate) {
        this.opsDelegate = opsDelegate;
    }

    /**
     * 操作委托类
     *
     * @return 操作委托类
     * @since v5.7.0
     */
    protected T opsDelegate() {
        return opsDelegate.get();
    }

    /**
     * 获取值
     *
     * @param value            值
     * @param defaultNullValue 当值为 {@code null} 时的默认值
     * @param <V>              值类型
     * @return 值, 当 value 为 {@code null} 且 defaultNullValue 为 {@code null} 时返回 {@code null}
     * @since v5.7.0
     */
    protected <V> V getValue(@Nullable V value, @Nullable Supplier<V> defaultNullValue) {
        return Objects.isNull(value) && Objects.nonNull(defaultNullValue) ? defaultNullValue.get() : value;
    }

    /**
     * 获取值
     *
     * @param value 值
     * @return 值
     * @since v5.7.0
     */
    protected boolean getBooleanValue(@Nullable Boolean value) {
        return Objects.nonNull(value) ? value : false;
    }

    /**
     * 获取值
     *
     * @param value 值
     * @return 值
     * @since v5.7.0
     */
    protected long getLongValue(@Nullable Boolean value) {
        return Objects.nonNull(value) ? 1L : -1L;
    }
}
