package com.persagy.apccontrol.basic.config.dao.check;

import com.persagy.apccontrol.basic.exception.enumeration.MybatisServiceErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 幂等性检查Service
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
public interface CheckIdempotencyService extends BaseCheckService {

    /**
     * 默认幂等性重复异常对象
     */
    Function<String, ApplicationException> DEFAULT_IDEMPOTENT_REPETITION_EXCEPTION =
            userTip -> new ApplicationException(MybatisServiceErrorEnum.IDEMPOTENT_REPETITION, userTip);

    /**
     * 检查幂等性
     *
     * @param isExists 是否存在
     * @throws ApplicationException 幂等性重复
     * @since v6.0.0
     */
    default void checkIdempotency(@Nullable Boolean isExists) throws ApplicationException {
        dynamicThrowError(Objects.nonNull(isExists) ? !isExists : null,
                () -> DEFAULT_IDEMPOTENT_REPETITION_EXCEPTION.apply(null));
    }

    /**
     * 检查幂等性
     *
     * @param isExists 结果数量
     * @param userTip  用户提示信息
     * @throws ApplicationException 幂等性重复
     * @since v6.0.0
     */
    default void checkIdempotency(@Nullable Boolean isExists, @Nullable String userTip) throws ApplicationException {
        dynamicThrowError(Objects.nonNull(isExists) ? !isExists : null,
                () -> DEFAULT_IDEMPOTENT_REPETITION_EXCEPTION.apply(userTip));
    }

    /**
     * 检查幂等性
     *
     * @param isExists 结果数量
     * @param e        当结果为 {@code null} 或 {@code false} 时抛出的结果, 不能为空
     * @throws ApplicationException 幂等性重复
     * @since v6.0.0
     */
    default void checkIdempotency(@Nullable Boolean isExists, @NonNull Supplier<ApplicationException> e)
            throws ApplicationException {
        dynamicThrowError(Objects.nonNull(isExists) ? !isExists : null, e);
    }

    /**
     * 检查幂等性
     *
     * @param resultCount 结果数量
     * @throws ApplicationException 幂等性重复
     * @since v6.0.0
     */
    default void checkIdempotency(@Nullable Long resultCount) throws ApplicationException {
        dynamicThrowError(Objects.nonNull(resultCount) ? resultCount < 1 : null,
                () -> DEFAULT_IDEMPOTENT_REPETITION_EXCEPTION.apply(null));
    }

    /**
     * 检查幂等性
     *
     * @param resultCount 结果数量
     * @param userTip     用户提示信息
     * @throws ApplicationException 幂等性重复
     * @since v6.0.0
     */
    default void checkIdempotency(@Nullable Long resultCount, @Nullable String userTip) throws ApplicationException {
        dynamicThrowError(Objects.nonNull(resultCount) ? resultCount < 1 : null,
                () -> DEFAULT_IDEMPOTENT_REPETITION_EXCEPTION.apply(userTip));
    }

    /**
     * 检查幂等性
     *
     * @param resultCount 结果数量
     * @param e           当结果为 {@code null} 或 {@code false} 时抛出的结果, 不能为空
     * @throws ApplicationException 幂等性重复
     * @since v6.0.0
     */
    default void checkIdempotency(@Nullable Long resultCount, @NonNull Supplier<ApplicationException> e)
            throws ApplicationException {
        dynamicThrowError(Objects.nonNull(resultCount) ? resultCount < 1 : null, e);
    }

    /**
     * 检查幂等性
     *
     * @param resultCount 结果数量
     * @throws ApplicationException 幂等性重复
     * @since v6.0.0
     */
    default void checkIdempotency(@Nullable Integer resultCount) throws ApplicationException {
        dynamicThrowError(Objects.nonNull(resultCount) ? resultCount < 1 : null,
                () -> DEFAULT_IDEMPOTENT_REPETITION_EXCEPTION.apply(null));
    }

    /**
     * 检查幂等性
     *
     * @param resultCount 结果数量
     * @param userTip     用户提示信息
     * @throws ApplicationException 幂等性重复
     * @since v6.0.0
     */
    default void checkIdempotency(@Nullable Integer resultCount, @Nullable String userTip) throws ApplicationException {
        dynamicThrowError(Objects.nonNull(resultCount) ? resultCount < 1 : null,
                () -> DEFAULT_IDEMPOTENT_REPETITION_EXCEPTION.apply(userTip));
    }

    /**
     * 检查幂等性
     *
     * @param resultCount 结果数量
     * @param e           当结果为 {@code null} 或 {@code false} 时抛出的结果, 不能为空
     * @throws ApplicationException 幂等性重复
     * @since v6.0.0
     */
    default void checkIdempotency(@Nullable Integer resultCount, @NonNull Supplier<ApplicationException> e)
            throws ApplicationException {
        dynamicThrowError(Objects.nonNull(resultCount) ? resultCount < 1 : null, e);
    }
}
