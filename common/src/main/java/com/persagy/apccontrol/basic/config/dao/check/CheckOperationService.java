package com.persagy.apccontrol.basic.config.dao.check;

import com.persagy.apccontrol.basic.exception.enumeration.MybatisServiceErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 操作检查Service
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
public interface CheckOperationService extends BaseCheckService {

    /**
     * 默认操作异常对象
     */
    Function<String, ApplicationException> DEFAULT_OPERATION_ERROR_EXCEPTION =
            userTip -> new ApplicationException(MybatisServiceErrorEnum.OPERATION_ERROR, userTip);

    /**
     * 通用结果检查
     *
     * @param operateResult 操作结果
     * @throws ApplicationException 执行操作失败
     * @since v6.0.0
     */
    default void checkOperation(@Nullable Boolean operateResult) throws ApplicationException {
        dynamicThrowError(operateResult, () -> DEFAULT_OPERATION_ERROR_EXCEPTION.apply(null));
    }

    /**
     * 通用结果检查
     *
     * @param operateResult 操作结果
     * @param userTip       默认用户提示信息
     * @throws ApplicationException 执行操作失败
     * @since v6.0.0
     */
    default void checkOperation(@Nullable Boolean operateResult, @Nullable String userTip) throws ApplicationException {
        dynamicThrowError(operateResult, () -> DEFAULT_OPERATION_ERROR_EXCEPTION.apply(userTip));
    }

    /**
     * 通用结果检查
     *
     * @param operateResult 操作结果
     * @param e             当结果为 {@code null} 或 {@code false} 时抛出的结果, 不能为空
     * @throws ApplicationException 自定义异常
     * @since v6.0.0
     */
    default void checkOperation(@Nullable Boolean operateResult, @NonNull Supplier<ApplicationException> e)
            throws ApplicationException {
        dynamicThrowError(operateResult, e);
    }
}
