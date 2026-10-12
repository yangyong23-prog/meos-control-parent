package com.persagy.apccontrol.basic.exception;

import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import org.jspecify.annotations.NonNull;

/**
 * 缓存对象模型业务异常工厂。
 *
 * <p>缓存对象模型跨越转换、资源服务和仓储层，统一在此创建带国际化文案的
 * {@link BusinessException}，避免各层使用不同异常类型或遗漏国际化。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public final class CacheObjectModelExceptionFactory {

    /**
     * 必填数据缺失国际化消息键。
     *
     * @since v7.260701.0
     */
    private static final @NonNull String REQUIRED_VALUE_MESSAGE_KEY =
            "key_41ae00b844f2453bb37df2246ee27d9c";

    /**
     * 数据结构非法国际化消息键。
     *
     * @since v7.260701.0
     */
    private static final @NonNull String INVALID_STRUCTURE_MESSAGE_KEY =
            "key_c5f182a198f04014bc9000d40d7186b9";

    /**
     * 缓存对象模型操作失败国际化消息键。
     *
     * @since v7.260701.0
     */
    private static final @NonNull String OPERATION_FAILED_MESSAGE_KEY =
            "key_4d50a60bfbab43c9af8cadbd4b886b1b";

    /**
     * 防止实例化缓存对象模型业务异常工厂。
     *
     * @since v7.260701.0
     */
    private CacheObjectModelExceptionFactory() {
        super();
    }

    /**
     * 创建必填数据缺失业务异常。
     *
     * @param valueName 缺失数据的稳定技术名称，不包含实际业务值
     * @return 国际化业务异常
     * @since v7.260701.0
     */
    public static @NonNull BusinessException requiredValue(@NonNull String valueName) {
        return new BusinessException(
                ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(),
                LocaleMessageUtil.getMessage(
                        CacheObjectModelExceptionFactory.REQUIRED_VALUE_MESSAGE_KEY,
                        valueName));
    }

    /**
     * 创建数据结构非法业务异常。
     *
     * @param dataName 非法数据结构的稳定技术名称，不包含实际业务值
     * @return 国际化业务异常
     * @since v7.260701.0
     */
    public static @NonNull BusinessException invalidStructure(@NonNull String dataName) {
        return new BusinessException(
                ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(),
                LocaleMessageUtil.getMessage(
                        CacheObjectModelExceptionFactory.INVALID_STRUCTURE_MESSAGE_KEY,
                        dataName));
    }

    /**
     * 创建缓存对象模型技术操作失败业务异常。
     *
     * <p>该入口用于 Redis、序列化等技术操作已经失败且需要跨应用边界返回统一文案的场景；
     * 原始异常必须作为 cause 保留，便于通过服务端日志定位根因。</p>
     *
     * @param operationName 失败操作的稳定技术名称，不包含实际业务值
     * @param cause         原始技术异常
     * @return 保留原始 cause 的国际化业务异常
     * @since v7.260701.0
     */
    public static @NonNull BusinessException operationFailed(
            @NonNull String operationName,
            @NonNull Throwable cause) {
        return new BusinessException(
                ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(),
                LocaleMessageUtil.getMessage(
                        CacheObjectModelExceptionFactory.OPERATION_FAILED_MESSAGE_KEY,
                        operationName),
                cause);
    }

    /**
     * 创建不带原始异常的缓存对象模型操作失败业务异常。
     *
     * <p>用于底层只返回失败标识、没有可传播异常的场景；具体失败类型和范围由调用方日志记录。</p>
     *
     * @param operationName 失败操作的稳定技术名称
     * @return 国际化业务异常
     * @since v7.260701.0
     */
    public static @NonNull BusinessException operationFailed(@NonNull String operationName) {
        return new BusinessException(
                ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(),
                LocaleMessageUtil.getMessage(
                        CacheObjectModelExceptionFactory.OPERATION_FAILED_MESSAGE_KEY,
                        operationName));
    }

}
