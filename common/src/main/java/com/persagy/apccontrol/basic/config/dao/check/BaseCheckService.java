package com.persagy.apccontrol.basic.config.dao.check;

import com.persagy.validate.utility.exception.ApplicationException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 检查Service基类
 *
 * <p>该类主要用于提供各类通用方法
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
public interface BaseCheckService {

    /**
     * 动态抛出异常
     *
     * @param operateResult 操作结果, 当值为 {@code null} 或 {@code false} 时抛出自定义异常
     * @param e             自定义异常对象
     * @throws ApplicationException 自定义异常
     * @since v6.0.0
     */
    default void dynamicThrowError(@Nullable Boolean operateResult,
                                   @NonNull Supplier<ApplicationException> e)
            throws ApplicationException {
        if (Objects.isNull(operateResult) || !operateResult) {
            Assert.notNull(e, "Exception must not be null");
            throw e.get();
        }
    }

    /**
     * 动态抛出异常
     *
     * @param result 结果
     * @param e      自定义异常对象
     * @param <T>    结果和返回类型
     * @return 结果
     * @throws ApplicationException 自定义异常
     * @since v6.0.0
     */
    default <T> T dynamicThrowError(@Nullable T result,
                                    @NonNull Supplier<ApplicationException> e)
            throws ApplicationException {
        if (Objects.isNull(result)) {
            Assert.notNull(e, "Exception must not be null");
            throw e.get();
        }
        return result;
    }
}
