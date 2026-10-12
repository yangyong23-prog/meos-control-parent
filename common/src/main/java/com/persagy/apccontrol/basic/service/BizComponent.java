package com.persagy.apccontrol.basic.service;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * 业务服务接口
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
public interface BizComponent<T> extends LogComponent {

    /**
     * 获取单条数据, 查询失败|数据为空时抛出异常
     *
     * @param source 数据源
     * @param <E>    数据类型
     * @return 单条数据
     * @throws BusinessException see {@code errorMessage}
     * @since 5.240402.0
     */
    default <E> E getOne(List<E> source) throws BusinessException {
        return getOne(true, source, LocaleMessageUtil.getMessage("key_609e3164b9dc496389d1a156b1f81174"));
    }

    /**
     * 获取单条数据
     *
     * @param isThrows 是否在查询失败|数据为空时抛出异常
     * @param source   数据源
     * @param <E>      数据类型
     * @return 单条数据
     * @throws BusinessException see {@code errorMessage}
     * @since 5.240402.0
     */
    default <E> E getOne(boolean isThrows, List<E> source) throws BusinessException {
        return getOne(isThrows, source, LocaleMessageUtil.getMessage("key_609e3164b9dc496389d1a156b1f81174"));
    }

    /**
     * 获取单条数据
     *
     * @param isThrows     是否在查询失败|数据为空时抛出异常
     * @param source       数据源
     * @param errorMessage 异常信息
     * @param <E>          数据类型
     * @return 单条数据
     * @throws BusinessException see {@code errorMessage}
     * @since v5.1.0
     */
    default <E> E getOne(boolean isThrows, List<E> source, String errorMessage) throws BusinessException {
        if (CollectionUtils.isNotEmpty(source)) {
            return source.get(0);
        }
        if (isThrows) {
            throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(), errorMessage);
        }
        return null;
    }

    /**
     * 获取CompletableFuture结果
     *
     * @param futures           CompletableFuture
     * @param handleResult      处理结果
     * @param exceptionSupplier 异常对象
     * @param <R>               结果类型
     * @param <CE>              CompletableFuture元素集合元素类型
     * @param <FC>              CompletableFuture元素集合类型
     * @param <E>               异常类型
     * @return CompletableFuture结果
     * @since 5.240601.0
     */
    default <R, CE,
            FC extends Collection<CompletableFuture<CE>>,
            E extends RuntimeException> R futureGet(
            @NonNull FC futures,
            @NonNull Supplier<R> resultSupplier,
            @NonNull BiConsumer<R, CE> handleResult,
            @NonNull Supplier<E> exceptionSupplier) {
        try {
            R result = resultSupplier.get();
            for (CompletableFuture<CE> future : futures) {
                handleResult.accept(result, future.get());
            }
            return result;
        } catch (InterruptedException e) {
            logProjectInstance().error("线程中断", e);
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            E exception = exceptionSupplier.get();
            logProjectInstance().error(exception.getLocalizedMessage(), e);
            throw exception;
        }
    }
}
