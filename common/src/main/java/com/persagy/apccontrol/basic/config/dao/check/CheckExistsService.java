package com.persagy.apccontrol.basic.config.dao.check;

import com.persagy.apccontrol.basic.exception.enumeration.MybatisServiceErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 是否存在检查Service
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
public interface CheckExistsService extends BaseCheckService {

    /**
     * 默认结果为空异常对象
     */
    Function<String, ApplicationException> DEFAULT_RESULT_NULL_EXCEPTION =
            userTip -> new ApplicationException(MybatisServiceErrorEnum.RESULT_IS_NULL, userTip);

    /**
     * 是否存在检查
     *
     * @param operateResult 是否存在结果
     * @throws ApplicationException ID异常
     * @since v6.0.0
     */
    default void checkExists(@Nullable Boolean operateResult) throws ApplicationException {
        dynamicThrowError(operateResult, () -> DEFAULT_RESULT_NULL_EXCEPTION.apply(null));
    }

    /**
     * 是否存在检查
     *
     * @param operateResult 是否存在结果
     * @param userTip       用户提示信息
     * @throws ApplicationException ID异常
     * @since v6.0.0
     */
    default void checkExists(@Nullable Boolean operateResult, @Nullable String userTip) throws ApplicationException {
        dynamicThrowError(operateResult, () -> DEFAULT_RESULT_NULL_EXCEPTION.apply(userTip));
    }

    /**
     * 是否存在检查
     *
     * @param result 结果
     * @param e      当结果为 {@code null} 或 {@code false} 时抛出的结果, 不能为空
     * @throws ApplicationException 自定义异常
     * @since v6.0.0
     */
    default void checkExists(@Nullable Boolean result, @NonNull Supplier<ApplicationException> e)
            throws ApplicationException {
        dynamicThrowError(result, e);
    }

    /**
     * 是否存在检查
     *
     * @param resultCount 结果数量
     * @throws ApplicationException ID异常
     * @since v6.0.0
     */
    default void checkExists(@Nullable Long resultCount) throws ApplicationException {
        dynamicThrowError(Objects.nonNull(resultCount) ? resultCount > 0 : null,
                () -> DEFAULT_RESULT_NULL_EXCEPTION.apply(null));
    }

    /**
     * 是否存在检查
     *
     * @param resultCount 结果数量
     * @param userTip     用户提示信息
     * @throws ApplicationException ID异常
     * @since v6.0.0
     */
    default void checkExists(@Nullable Long resultCount, @Nullable String userTip) throws ApplicationException {
        dynamicThrowError(Objects.nonNull(resultCount) ? resultCount > 0 : null,
                () -> DEFAULT_RESULT_NULL_EXCEPTION.apply(userTip));
    }

    /**
     * 是否存在检查
     *
     * @param resultCount 结果数量
     * @param e           当结果为 {@code null} 或 {@code false} 时抛出的结果, 不能为空
     * @throws ApplicationException 自定义异常
     * @since v6.0.0
     */
    default void checkExists(@Nullable Long resultCount, @NonNull Supplier<ApplicationException> e)
            throws ApplicationException {
        dynamicThrowError(Objects.nonNull(resultCount) ? resultCount > 0 : null, e);
    }

    /**
     * 是否存在检查
     *
     * @param resultCount 结果数量
     * @throws ApplicationException ID异常
     * @since v6.0.0
     */
    default void checkExists(@Nullable Integer resultCount) throws ApplicationException {
        dynamicThrowError(Objects.nonNull(resultCount) ? resultCount > 0 : null,
                () -> DEFAULT_RESULT_NULL_EXCEPTION.apply(null));
    }

    /**
     * 是否存在检查
     *
     * @param resultCount 结果数量
     * @param userTip     用户提示信息
     * @throws ApplicationException ID异常
     * @since v6.0.0
     */
    default void checkExists(@Nullable Integer resultCount, @Nullable String userTip) throws ApplicationException {
        dynamicThrowError(Objects.nonNull(resultCount) ? resultCount > 0 : null,
                () -> DEFAULT_RESULT_NULL_EXCEPTION.apply(userTip));
    }

    /**
     * 是否存在检查
     *
     * @param resultCount 结果数量
     * @param e           当结果为 {@code null} 或 {@code false} 时抛出的结果, 不能为空
     * @throws ApplicationException 自定义异常
     * @since v6.0.0
     */
    default void checkExists(@Nullable Integer resultCount, @NonNull Supplier<ApplicationException> e)
            throws ApplicationException {
        dynamicThrowError(Objects.nonNull(resultCount) ? resultCount > 0 : null, e);
    }

    /**
     * 是否存在检查
     *
     * @param result 结果
     * @param <T>    结果类型
     * @return 结果
     * @throws ApplicationException 结果为空
     * @since v6.0.0
     */
    default <T> T checkExistsAndGet(@Nullable T result) throws ApplicationException {
        return dynamicThrowError(result, () -> DEFAULT_RESULT_NULL_EXCEPTION.apply(null));
    }

    /**
     * 是否存在检查
     *
     * @param result  结果
     * @param userTip 用户提示信息
     * @param <T>     结果类型
     * @return 结果
     * @throws ApplicationException 结果为空
     * @since v6.0.0
     */
    default <T> T checkExistsAndGet(@Nullable T result, @Nullable String userTip) throws ApplicationException {
        return dynamicThrowError(result, () -> DEFAULT_RESULT_NULL_EXCEPTION.apply(userTip));
    }

    /**
     * 是否存在检查
     *
     * @param result 结果
     * @param <T>    结果类型
     * @param e      当结果为 {@code null} 或 {@code false} 时抛出的结果, 不能为空
     * @return 结果
     * @throws ApplicationException 自定义异常
     * @since v6.0.0
     */
    default <T> T checkExistsAndGet(@Nullable T result, @NonNull Supplier<ApplicationException> e)
            throws ApplicationException {
        return dynamicThrowError(result, e);
    }
}
