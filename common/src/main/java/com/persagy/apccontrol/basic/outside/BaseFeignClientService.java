package com.persagy.apccontrol.basic.outside;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import feign.FeignException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 基础调用客户端服务
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
public interface BaseFeignClientService extends LogComponent {

    /**
     * 获取集团编码
     *
     * @return 集团编码
     * @since v5.7.0
     */
    default String getGroupCode() {
        return ContextUtil.getGroupCode();
    }

    /**
     * 获取集团编码
     *
     * @return 集团编码
     * @since 5.240801.0
     */
    default String getProjectId() {
        return ContextUtil.getProjectId();
    }

    /**
     * 获取单条数据
     *
     * @param isThrows 是否在查询失败|数据为空时抛出异常
     * @param source   数据源
     * @param <T>      数据类型
     * @return 单条数据
     * @throws BusinessException see {@code errorMessage}
     * @since v5.7.0
     */
    default <T> T getOne(boolean isThrows, List<T> source) throws BusinessException {
        return getOne(isThrows, source, LocaleMessageUtil.getMessage("key_609e3164b9dc496389d1a156b1f81174"));
    }

    /**
     * 获取单条数据
     *
     * @param isThrows     是否在查询失败|数据为空时抛出异常
     * @param source       数据源
     * @param errorMessage 异常信息
     * @param <T>          数据类型
     * @return 单条数据
     * @throws BusinessException see {@code errorMessage}
     * @since v5.1.0
     */
    default <T> T getOne(boolean isThrows, List<T> source, String errorMessage) throws BusinessException {
        if (CollectionUtils.isNotEmpty(source)) {
            return source.get(0);
        }
        if (isThrows) {
            throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(), errorMessage);
        }
        return null;
    }

    /**
     * 通用查询方法
     *
     * @param isThrows      是否在查询失败|数据为空时抛出异常
     * @param instanceType  实例类型
     * @param param         请求参数
     * @param executor      执行函数式接口
     * @param defaultResult 默认返回值
     * @param <P>           请求参数类型
     * @param <R>           查询响应值类型
     * @return 查询结果
     * @since 5.240801.0
     */
    default <P, R> R query(
            boolean isThrows, @NonNull String instanceType, @NonNull P param,
            @NonNull Supplier<CustomerResponse<R>> executor,
            @Nullable Supplier<R> defaultResult) {
        return query(isThrows, null, null, instanceType,
                param, executor, CustomerResponse::getContent, defaultResult);
    }

    /**
     * 通用查询方法
     *
     * @param isThrows      是否在查询失败|数据为空时抛出异常
     * @param groupCode     集团编码
     * @param projectId     项目ID
     * @param instanceType  实例类型
     * @param param         请求参数
     * @param executor      执行函数式接口
     * @param defaultResult 默认返回值
     * @param <P>           请求参数类型
     * @param <R>           查询响应值类型
     * @return 查询结果
     * @since v5.8.0
     */
    default <P, R> R query(
            boolean isThrows, @Nullable String groupCode, @Nullable String projectId,
            @NonNull String instanceType, @NonNull P param,
            @NonNull Supplier<CustomerResponse<R>> executor,
            @Nullable Supplier<R> defaultResult) {
        return query(isThrows, groupCode, projectId, instanceType, param,
                executor, CustomerResponse::getContent, defaultResult);
    }

    /**
     * 通用查询数量方法
     *
     * @param isThrows      是否在查询失败|数据为空时抛出异常
     * @param instanceType  实例类型
     * @param param         请求参数
     * @param executor      执行函数式接口
     * @param defaultResult 默认返回值
     * @param <P>           请求参数类型
     * @param <R>           查询响应值类型
     * @return 查询结果
     * @since 5.240801.0
     */
    default <P, R> Integer queryCount(
            boolean isThrows, @NonNull String instanceType, @NonNull P param,
            @NonNull Supplier<CustomerResponse<R>> executor,
            @Nullable Supplier<Integer> defaultResult) {
        return queryCount(isThrows, null, null, instanceType,
                param, executor, CustomerResponse::getTotal, defaultResult);
    }

    /**
     * 通用查询数量方法
     *
     * @param isThrows      是否在查询失败|数据为空时抛出异常
     * @param groupCode     集团编码
     * @param projectId     项目ID
     * @param instanceType  实例类型
     * @param param         请求参数
     * @param executor      执行函数式接口
     * @param defaultResult 默认返回值
     * @param <P>           请求参数类型
     * @param <R>           查询响应值类型
     * @return 查询结果
     * @since 5.240602.0
     */
    default <P, R> Integer queryCount(
            boolean isThrows, @Nullable String groupCode, @Nullable String projectId,
            @NonNull String instanceType, @NonNull P param,
            @NonNull Supplier<CustomerResponse<R>> executor,
            @Nullable Supplier<Integer> defaultResult) {
        return queryCount(isThrows, groupCode, projectId, instanceType, param,
                executor, CustomerResponse::getTotal, defaultResult);
    }

    /**
     * 通用查询方法
     *
     * @param isThrows        是否在查询失败|数据为空时抛出异常
     * @param groupCode       集团编码
     * @param projectId       项目ID
     * @param instanceType    实例类型
     * @param param           请求参数
     * @param executor        执行函数式接口
     * @param successCallback 执行成功函数式接口
     * @param defaultResult   默认返回值
     * @param <P>             请求参数类型
     * @param <R>             查询响应值类型
     * @param <RR>            方法响应值类型
     * @return 查询结果
     * @since v5.6.0
     */
    default <P, R, RR> RR query(
            boolean isThrows, @Nullable String groupCode, @Nullable String projectId,
            @NonNull String instanceType, @NonNull P param,
            @NonNull Supplier<CustomerResponse<R>> executor,
            @NonNull Function<CustomerResponse<R>, RR> successCallback,
            @Nullable Supplier<RR> defaultResult) {
        CustomerResponse<R> response = baseQuery(isThrows, groupCode, projectId, instanceType, param, executor);
        if (Objects.nonNull(response)) {
            return successCallback.apply(response);
        }
        if (Objects.nonNull(defaultResult)) {
            return defaultResult.get();
        }
        return null;
    }

    /**
     * 通用查询数量方法
     *
     * @param isThrows        是否在查询失败|数据为空时抛出异常
     * @param groupCode       集团编码
     * @param projectId       项目ID
     * @param instanceType    实例类型
     * @param param           请求参数
     * @param executor        执行函数式接口
     * @param successCallback 执行成功函数式接口
     * @param defaultResult   默认返回值
     * @param <P>             请求参数类型
     * @param <R>             查询响应值类型
     * @return 查询结果
     * @since 5.240602.0
     */
    default <P, R> Integer queryCount(
            boolean isThrows, @Nullable String groupCode, @Nullable String projectId,
            @NonNull String instanceType, @NonNull P param,
            @NonNull Supplier<CustomerResponse<R>> executor,
            @NonNull Function<CustomerResponse<R>, Integer> successCallback,
            @Nullable Supplier<Integer> defaultResult) {
        CustomerResponse<R> response = baseQueryCount(isThrows, groupCode, projectId, instanceType, param, executor);
        if (Objects.nonNull(response)) {
            return successCallback.apply(response);
        }
        if (Objects.nonNull(defaultResult)) {
            return defaultResult.get();
        }
        return null;
    }

    /**
     * 通用新增方法
     *
     * @param isThrows     是否在查询失败|数据为空时抛出异常
     * @param instanceType 实例类型
     * @param param        请求参数
     * @param executor     执行函数式接口
     * @param <P>          请求参数类型
     * @param <R>          响应值类型
     * @return 查询结果
     * @since 5.240801.0
     */
    @Nullable
    default <P, R> R insert(
            boolean isThrows, @NonNull String instanceType, @NonNull P param,
            @NonNull Supplier<CustomerResponse<R>> executor) {
        return operation(isThrows, null, null, LocaleMessageUtil.getMessage("key_6cc5c1e16b074d669b45cdbd4f5a5d91"), instanceType, param, executor);
    }

    /**
     * 通用新增方法
     *
     * @param isThrows     是否在查询失败|数据为空时抛出异常
     * @param groupCode    集团编码
     * @param projectId    项目ID
     * @param instanceType 实例类型
     * @param param        请求参数
     * @param executor     执行函数式接口
     * @param <P>          请求参数类型
     * @param <R>          响应值类型
     * @return 查询结果
     * @since v5.6.0
     */
    @Nullable
    default <P, R> R insert(
            boolean isThrows, @Nullable String groupCode, @NonNull String projectId,
            @NonNull String instanceType, @NonNull P param,
            @NonNull Supplier<CustomerResponse<R>> executor) {
        return operation(isThrows, groupCode, projectId, LocaleMessageUtil.getMessage("key_6cc5c1e16b074d669b45cdbd4f5a5d91"), instanceType, param, executor);
    }

    /**
     * 通用编辑方法
     *
     * @param isThrows     是否在查询失败|数据为空时抛出异常
     * @param instanceType 实例类型
     * @param param        请求参数
     * @param executor     执行函数式接口
     * @param <P>          请求参数类型
     * @param <R>          响应值类型
     * @return 查询结果
     * @since 5.240801.0
     */
    @Nullable
    default <P, R> R update(
            boolean isThrows, @NonNull String instanceType, @NonNull P param,
            @NonNull Supplier<CustomerResponse<R>> executor) {
        return operation(isThrows, null, null, LocaleMessageUtil.getMessage("key_ddc82084c21541ea820840e9338664b9"), instanceType, param, executor);
    }

    /**
     * 通用编辑方法
     *
     * @param isThrows     是否在查询失败|数据为空时抛出异常
     * @param groupCode    集团编码
     * @param projectId    项目ID
     * @param instanceType 实例类型
     * @param param        请求参数
     * @param executor     执行函数式接口
     * @param <P>          请求参数类型
     * @param <R>          响应值类型
     * @return 查询结果
     * @since v5.6.0
     */
    @Nullable
    default <P, R> R update(
            boolean isThrows, @Nullable String groupCode, @NonNull String projectId,
            @NonNull String instanceType, @NonNull P param,
            @NonNull Supplier<CustomerResponse<R>> executor) {
        return operation(isThrows, groupCode, projectId, LocaleMessageUtil.getMessage("key_ddc82084c21541ea820840e9338664b9"), instanceType, param, executor);
    }

    /**
     * 通用删除方法
     *
     * @param isThrows     是否在查询失败|数据为空时抛出异常
     * @param instanceType 实例类型
     * @param param        请求参数
     * @param executor     执行函数式接口
     * @param <P>          请求参数类型
     * @param <R>          响应值类型
     * @return 查询结果
     * @since 5.240801.0
     */
    @Nullable
    default <P, R> R delete(
            boolean isThrows, @NonNull String instanceType, @NonNull P param,
            @NonNull Supplier<CustomerResponse<R>> executor) {
        return operation(isThrows, null, null, LocaleMessageUtil.getMessage("key_c6652b85ecbe4dd6ba39de79ecdee66a"), instanceType, param, executor);
    }

    /**
     * 通用删除方法
     *
     * @param isThrows     是否在查询失败|数据为空时抛出异常
     * @param groupCode    集团编码
     * @param projectId    项目ID
     * @param instanceType 实例类型
     * @param param        请求参数
     * @param executor     执行函数式接口
     * @param <P>          请求参数类型
     * @param <R>          响应值类型
     * @return 查询结果
     * @since v5.7.0
     */
    @Nullable
    default <P, R> R delete(
            boolean isThrows, @Nullable String groupCode, @NonNull String projectId,
            @NonNull String instanceType, @NonNull P param,
            @NonNull Supplier<CustomerResponse<R>> executor) {
        return operation(isThrows, groupCode, projectId, LocaleMessageUtil.getMessage("key_c6652b85ecbe4dd6ba39de79ecdee66a"), instanceType, param, executor);
    }

    /**
     * 通用操作方法
     *
     * @param isThrows      是否在查询失败|数据为空时抛出异常
     * @param groupCode     集团编码
     * @param projectId     项目ID
     * @param operationType 操作类型
     * @param instanceType  实例类型
     * @param param         请求参数
     * @param executor      执行函数式接口
     * @param <P>           请求参数类型
     * @param <R>           响应值类型
     * @return 查询结果
     * @since v5.7.0
     */
    @Nullable
    default <P, R> R operation(
            boolean isThrows, @Nullable String groupCode, @Nullable String projectId,
            @NonNull String operationType, @NonNull String instanceType, @NonNull P param,
            @NonNull Supplier<CustomerResponse<R>> executor) {
        return operation(isThrows, groupCode, projectId, operationType, instanceType, param, executor, null);
    }

    /**
     * 通用操作方法
     *
     * @param isThrows            是否在查询失败|数据为空时抛出异常
     * @param groupCode           集团编码
     * @param projectId           项目ID
     * @param operationType       操作类型
     * @param instanceType        实例类型
     * @param param               请求参数
     * @param executor            执行函数式接口
     * @param defaultIfResultNull 结果为空时的默认值
     * @param <P>                 请求参数类型
     * @param <R>                 响应值类型
     * @return 查询结果
     * @since v5.7.0
     */
    @Nullable
    default <P, R> R operation(
            boolean isThrows, @Nullable String groupCode, @Nullable String projectId,
            @NonNull String operationType, @NonNull String instanceType, @NonNull P param,
            @NonNull Supplier<CustomerResponse<R>> executor, @Nullable Supplier<R> defaultIfResultNull) {
        groupCode = StringUtils.defaultIfBlank(groupCode, getGroupCode());
        projectId = StringUtils.defaultIfBlank(projectId, getProjectId());
        defaultIfResultNull = Objects.nonNull(defaultIfResultNull) ? defaultIfResultNull : () -> null;
        CustomerResponse<R> response = baseOperation(isThrows, groupCode, projectId,
                operationType, instanceType, param, executor);
        if (Objects.isNull(response)) {
            return null;
        }
        R object = response.getContent();
        return ObjectUtils.isNotEmpty(object) ? object : defaultIfResultNull.get();
    }

    /**
     * 通用查询方法
     *
     * @param isThrows     是否在查询失败|数据为空时抛出异常
     * @param groupCode    集团编码
     * @param projectId    项目ID
     * @param instanceType 实例类型
     * @param param        请求参数
     * @param executor     执行函数式接口
     * @param <P>          请求参数类型
     * @param <R>          响应值类型
     * @return 查询结果
     * @since v5.6.0
     */
    @Nullable
    default <P, R> CustomerResponse<R> baseQuery(
            boolean isThrows, @Nullable String groupCode, @Nullable String projectId,
            @NonNull String instanceType, @NonNull P param,
            @NonNull Supplier<CustomerResponse<R>> executor) {
        groupCode = StringUtils.defaultIfBlank(groupCode, getGroupCode());
        projectId = StringUtils.defaultIfBlank(projectId, getProjectId());
        CustomerResponse<R> response = baseOperation(isThrows, groupCode, projectId,
                LocaleMessageUtil.getMessage("key_52d5dacff1c948999cd789b11fe23be9"), instanceType, param, executor);
        if (Objects.isNull(response)) {
            return null;
        }
        R object = response.getContent();
        if (ObjectUtils.isNotEmpty(object)) {
            if (log().isTraceEnabled()) {
                LogExecutor.rpc(groupCode, param, response).trace(log(), logTag(),
                        "查询 [" + instanceType + "] 成功");
            }
            return response;
        }
        List<Pair<String, Object>> additionalMessages = StringUtils.isNotBlank(projectId)
                ? Collections.singletonList(Pair.of("projectId", projectId)) : null;
        LogExecutor.rpc(groupCode, param, response).warn(log(), logTag(),
                "未查询到 [" + instanceType + "]", additionalMessages);
        if (isThrows) {
            throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(), LocaleMessageUtil.getMessage("key_99b6648ad3694b36b2f81c82a935eced") + instanceType + "]");
        }
        return null;
    }

    /**
     * 通用查询数量方法
     *
     * @param isThrows     是否在查询失败|数据为空时抛出异常
     * @param groupCode    集团编码
     * @param projectId    项目ID
     * @param instanceType 实例类型
     * @param param        请求参数
     * @param executor     执行函数式接口
     * @param <P>          请求参数类型
     * @param <R>          响应值类型
     * @return 查询数量
     * @since v5.6.0
     */
    @Nullable
    default <P, R> CustomerResponse<R> baseQueryCount(
            boolean isThrows, @Nullable String groupCode, @Nullable String projectId,
            @NonNull String instanceType, @NonNull P param,
            @NonNull Supplier<CustomerResponse<R>> executor) {
        groupCode = StringUtils.defaultString(groupCode, getGroupCode());
        CustomerResponse<R> response = baseOperation(isThrows, groupCode, projectId,
                LocaleMessageUtil.getMessage("key_52d5dacff1c948999cd789b11fe23be9"), instanceType, param, executor);
        if (Objects.isNull(response)) {
            return null;
        }
        Integer total = response.getTotal();
        if (Objects.nonNull(total)) {
            if (log().isTraceEnabled()) {
                LogExecutor.rpc(groupCode, param, response).trace(log(), logTag(),
                        "查询 [" + instanceType + "] 数量成功");
            }
            return response;
        }
        List<Pair<String, Object>> additionalMessages = StringUtils.isNotBlank(projectId)
                ? Collections.singletonList(Pair.of("projectId", projectId)) : null;
        LogExecutor.rpc(groupCode, param, response).warn(log(), logTag(),
                "未查询到 [" + instanceType + "] 数量", additionalMessages);
        if (isThrows) {
            throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(), LocaleMessageUtil.getMessage("key_99b6648ad3694b36b2f81c82a935eced") + instanceType + LocaleMessageUtil.getMessage("key_11006f169a334501a4ff9ba83beb1bef"));
        }
        return null;
    }

    /**
     * 基础操作方法
     *
     * @param isThrows      是否在查询失败|数据为空时抛出异常
     * @param groupCode     集团编码
     * @param projectId     项目ID
     * @param operationType 操作类型
     * @param instanceType  实例类型
     * @param param         请求参数
     * @param executor      执行函数式接口
     * @param <P>           请求参数类型
     * @param <R>           响应值类型
     * @return 查询结果
     * @since v5.6.0
     */
    @Nullable
    default <P, R> CustomerResponse<R> baseOperation(
            boolean isThrows, @NonNull String groupCode, @Nullable String projectId,
            @NonNull String operationType, @NonNull String instanceType, @NonNull P param,
            @NonNull Supplier<CustomerResponse<R>> executor) {
        CustomerResponse<R> objectResponse;
        try {
            objectResponse = executor.get();
        } catch (FeignException e) {
            List<Pair<String, Object>> additionalMessages = StringUtils.isNotBlank(projectId)
                    ? Collections.singletonList(Pair.of("projectId", projectId)) : null;
            Object response = e.responseBody().map(item -> new String(item.array())).orElse(null);
            LogExecutor.rpc(groupCode, param, response).error(log(), logTag(),
                    operationType + " [" + instanceType + "] 失败", e, additionalMessages);
            if (isThrows) {
                throw e;
            }
            return null;
        }
        return objectResponse;
    }
}
