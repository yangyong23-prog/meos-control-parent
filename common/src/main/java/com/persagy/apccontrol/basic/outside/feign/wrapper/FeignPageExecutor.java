package com.persagy.apccontrol.basic.outside.feign.wrapper;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.model.page.DefaultListPage;
import com.persagy.apccontrol.basic.opt.OptPagedFetch;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import com.persagy.apccontrol.basic.model.page.Pageable;
import com.persagy.apccontrol.basic.model.page.query.PageQuery;
import com.persagy.apccontrol.basic.outside.dto.DmpResult;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdPageQuery;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.validate.utility.enumeration.error.client.RequestServerErrorEnum;
import com.persagy.validate.utility.enumeration.error.third.MiddlewareServerErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Feign分页执行器
 *
 * @author JiYinchuan
 * @since 5.240901.0
 */
public class FeignPageExecutor {

    private static int DEFAULT_PAGE_SIZE_LIMIT;

    static {
        try {
            DEFAULT_PAGE_SIZE_LIMIT = Integer.parseInt(SpringUtil.getProperty("page.size.limit", "1000"));
        } catch (Exception e) {
            DEFAULT_PAGE_SIZE_LIMIT = 1000;
        }
    }

    /**
     * 执行分页
     *
     * <p>兼容旧版响应, 后续可以逐步废弃</p>
     *
     * @param pageQuery 分页参数
     * @param callback  执行回调
     * @param <T>       结果元素类型
     * @return 查询结果
     * @since 7.250201.0
     * @deprecated {@link #execute(DtpRwdPageQuery, Function)}
     */
    @Deprecated
    public static <T> DmpResult<List<T>> executeToDmpResult(
            @Nullable DtpRwdPageQuery pageQuery,
            @NonNull Function<PageQuery, DmpResult<List<T>>> callback) {
        return executeToDmpResult(Objects.nonNull(pageQuery) ? pageQuery.toPageQuery() : null, callback);
    }

    /**
     * 执行分页
     *
     * <p>兼容旧版响应, 后续可以逐步废弃</p>
     *
     * @param pageQuery 分页参数
     * @param callback  执行回调
     * @param <T>       结果元素类型
     * @return 查询结果
     * @since 7.250201.0
     * @deprecated {@link #execute(PageQuery, Function)}
     */
    @Deprecated
    public static <T> DmpResult<List<T>> executeToDmpResult(
            @Nullable PageQuery pageQuery,
            @NonNull Function<PageQuery, DmpResult<List<T>>> callback) {
        return executeToDmpResult(pageQuery, callback, DEFAULT_PAGE_SIZE_LIMIT);
    }

    /**
     * 执行分页
     *
     * @param pageQuery 分页参数
     * @param callback  执行回调
     * @param <T>       结果元素类型
     * @return 查询结果
     * @since 7.250201.0
     */
    public static <T> CustomerResponse<List<T>> execute(
            @Nullable DtpRwdPageQuery pageQuery,
            @NonNull Function<PageQuery, CustomerResponse<List<T>>> callback) {
        return execute(Objects.nonNull(pageQuery) ? pageQuery.toPageQuery() : null, callback);
    }

    /**
     * 执行分页
     *
     * @param pageQuery 分页参数
     * @param callback  执行回调
     * @param <T>       结果元素类型
     * @return 查询结果
     * @since 7.250201.0
     */
    public static <T> CustomerResponse<List<T>> execute(
            @Nullable PageQuery pageQuery,
            @NonNull Function<PageQuery, CustomerResponse<List<T>>> callback) {
        return execute(pageQuery, callback, DEFAULT_PAGE_SIZE_LIMIT);
    }

    /**
     * 执行分页
     *
     * @param pageQuery 分页参数
     * @param callback  执行回调
     * @param <T>       结果元素类型
     * @return 查询结果
     * @since 5.240901.0
     */
    public static <T> CustomerResponse<List<T>> execute(
            @Nullable PageQuery pageQuery,
            @NonNull Function<PageQuery, CustomerResponse<List<T>>> callback, int limit) {
        // 当分页参数为空时, 递归执行分页逻辑
        if (Objects.isNull(pageQuery) || ObjectUtils.allNull(pageQuery.getPageNo(), pageQuery.getPageSize())) {
            // opt B10：首发也按 limit 分页，不再一次请求 Integer.MAX_VALUE 条
            if (OptSwitch.on(OptSwitch.FEIGN_PAGE_FIRST_LIMIT)) {
                return OptPagedFetch.fetchAll(limit,
                        (no, size) -> callback.apply(new PageQuery(no, size)),
                        CustomerResponse::getContent, CustomerResponse::getTotal,
                        (r, all) -> {
                            r.setContent(all);
                            r.setCount(all.size());
                        },
                        () -> execute(new PageQuery(1, Integer.MAX_VALUE), callback, limit),
                        BusinessThreadPoolExecutor.ttlExecutor());
            }
            if (OptPagedFetch.MODE_VERIFY.equals(OptSwitch.value(OptSwitch.FEIGN_PAGE_FIRST_LIMIT, ""))) {
                // verify：返回原逻辑结果，另按分页查一次比对条数
                CustomerResponse<List<T>> legacy = execute(new PageQuery(1, Integer.MAX_VALUE), callback, limit);
                return OptPagedFetch.verify(legacy,
                        () -> OptPagedFetch.fetchAll(limit,
                                (no, size) -> callback.apply(new PageQuery(no, size)),
                                CustomerResponse::getContent, CustomerResponse::getTotal,
                                (r, all) -> {
                                    r.setContent(all);
                                    r.setCount(all.size());
                                },
                                // 下游无 total 时分页本就退回原逻辑，视为一致
                                () -> legacy, null),
                        CustomerResponse::getContent, "execute");
            }
            return execute(new PageQuery(1, Integer.MAX_VALUE), callback, limit);
        }
        // 当仅查询数量时, 直接执行
        if (BooleanUtils.isTrue(pageQuery.getOnlyCount())) {
            return callback.apply(null);
        }
        Integer pageNo = pageQuery.getPageNo();
        Integer pageSize = pageQuery.getPageSize();
        if (ObjectUtils.anyNull(pageNo, pageSize)) {
            throw new ApplicationException(RequestServerErrorEnum.SERVICE_ERROR, LocaleMessageUtil.getMessage("key_f1539eb02efd4546b626990a684b2fe5"));
        }
        // 当查询条数小于等于阈值时, 直接执行
        if (pageSize <= limit) {
            return callback.apply(pageQuery);
        }
        CustomerResponse<List<T>> response = callback.apply(pageQuery);
        List<T> allContent = Objects.nonNull(response.getContent())
                ? Collections.synchronizedList(response.getContent())
                : null;
        // 当结果数据量大于等于总数时直接跳过执行, 以兼容未强制分页的情况
        if (Objects.nonNull(allContent)
                && Objects.nonNull(response.getTotal())
                && allContent.size() >= response.getTotal()) {
            return response;
        }
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        Pageable<List<T>> pageable = new DefaultListPage<>(pageQuery, response.getTotal(), allContent);
        for (int i = (pageable.getPageNo() + 1); i <= pageable.getTotalPage(); i++) {
            PageQuery realPageQuery = new PageQuery(i, limit);
            Runnable runnable = () -> {
                CustomerResponse<List<T>> eachResponse = callback.apply(realPageQuery);
                if (Objects.nonNull(eachResponse)) {
                    List<T> content = eachResponse.getContent();
                    if (CollectionUtils.isNotEmpty(content)) {
                        allContent.addAll(content);
                    }
                }
            };
            futures.add(CompletableFuture.runAsync(runnable, BusinessThreadPoolExecutor.ttlExecutor()));
        }
        if (CollectionUtils.isNotEmpty(futures)) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
        response.setContent(allContent);
        response.setCount(allContent.size());
        return response;
    }

    /**
     * 执行分页
     *
     * @param pageQuery 分页参数
     * @param callback  执行回调
     * @param <T>       结果元素类型
     * @return 查询结果
     * @since 5.240901.0
     */
    public static <T> DmpResult<List<T>> executeToDmpResult(
            @Nullable PageQuery pageQuery,
            @NonNull Function<PageQuery, DmpResult<List<T>>> callback, int limit) {
        // 当分页参数为空时, 递归执行分页逻辑
        if (Objects.isNull(pageQuery) || ObjectUtils.allNull(pageQuery.getPageNo(), pageQuery.getPageSize())) {
            // opt B10：首发也按 limit 分页；与原逻辑一致，剩余页顺序请求
            if (OptSwitch.on(OptSwitch.FEIGN_PAGE_FIRST_LIMIT)) {
                return OptPagedFetch.fetchAll(limit,
                        (no, size) -> callback.apply(new PageQuery(no, size)),
                        DmpResult::getData, DmpResult::getTotal,
                        (r, all) -> {
                            r.setData(all);
                            r.setCount(all.size());
                        },
                        () -> executeToDmpResult(new PageQuery(1, Integer.MAX_VALUE), callback, limit),
                        null);
            }
            if (OptPagedFetch.MODE_VERIFY.equals(OptSwitch.value(OptSwitch.FEIGN_PAGE_FIRST_LIMIT, ""))) {
                DmpResult<List<T>> legacy = executeToDmpResult(new PageQuery(1, Integer.MAX_VALUE), callback, limit);
                return OptPagedFetch.verify(legacy,
                        () -> OptPagedFetch.fetchAll(limit,
                                (no, size) -> callback.apply(new PageQuery(no, size)),
                                DmpResult::getData, DmpResult::getTotal,
                                (r, all) -> {
                                    r.setData(all);
                                    r.setCount(all.size());
                                },
                                // 下游无 total 时分页本就退回原逻辑，视为一致
                                () -> legacy, null),
                        DmpResult::getData, "executeToDmpResult");
            }
            return executeToDmpResult(new PageQuery(1, Integer.MAX_VALUE), callback, limit);
        }
        // 当仅查询数量时, 直接执行
        if (BooleanUtils.isTrue(pageQuery.getOnlyCount())) {
            return callback.apply(null);
        }
        Integer pageNo = pageQuery.getPageNo();
        Integer pageSize = pageQuery.getPageSize();
        if (ObjectUtils.anyNull(pageNo, pageSize)) {
            throw new ApplicationException(RequestServerErrorEnum.SERVICE_ERROR, LocaleMessageUtil.getMessage("key_f1539eb02efd4546b626990a684b2fe5"));
        }
        // 当查询条数小于等于阈值时, 直接执行
        if (pageSize <= limit) {
            return callback.apply(pageQuery);
        }
        DmpResult<List<T>> response = callback.apply(pageQuery);
        Pageable<List<T>> pageable = new DefaultListPage<>(pageQuery, response.getTotal(), response.getData());
        List<T> allContent = pageable.getRecords();
        for (int i = (pageable.getPageNo() + 1); i <= pageable.getTotalPage(); i++) {
            PageQuery realPageQuery = new PageQuery(i, limit);
            response = callback.apply(realPageQuery);
            Integer count = response.getCount();
            Integer total = response.getTotal();
            List<T> content = response.getData();
            if (Objects.isNull(count)) {
                throw new ApplicationException(MiddlewareServerErrorEnum.RPC_SERVER_ERROR, LocaleMessageUtil.getMessage("key_3a1be0d9a994460ba551ff8cdede97a2"));
            }
            if (Objects.isNull(total)) {
                throw new ApplicationException(MiddlewareServerErrorEnum.RPC_SERVER_ERROR, LocaleMessageUtil.getMessage("key_e0b244d06aa94af69e1b9ce1580a07a2"));
            }
            if (CollectionUtils.isNotEmpty(content)) {
                allContent.addAll(content);
            }
        }
        response.setData(allContent);
        response.setCount(allContent.size());
        return response;
    }
}
