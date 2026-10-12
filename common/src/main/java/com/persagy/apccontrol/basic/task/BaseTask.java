package com.persagy.apccontrol.basic.task;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.dto.GroupDTO;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.outside.feign.service.MidPermissionClientService;
import com.persagy.framework.common.context.ContextConstants;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.executor.ThreadExecutor;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.MDC;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 基础任务
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Slf4j
public abstract class BaseTask implements LogComponent {

    /**
     * 根据上下文执行集团
     *
     * @param callback 集团执行回调
     * @throws InterruptedException 线程中断
     * @since 7.250801.0
     */
    protected void handlerGroupByContext(
            @NonNull FailCallbackConsumer<GroupDTO> callback)
            throws InterruptedException {
        handlerGroup(parseFilterGroupCodesFromContext(), callback);
    }

    /**
     * 执行集团
     *
     * @param filterGroupCodes 过滤集团编码, 为空时执行所有集团
     * @param callback         集团执行回调
     * @throws InterruptedException 线程中断
     * @since v6.0.0
     */
    protected void handlerGroup(
            @Nullable Set<String> filterGroupCodes,
            @NonNull FailCallbackConsumer<GroupDTO> callback)
            throws InterruptedException {
        handlerGroup(filterGroupCodes, group -> {
            callback.accept(group);
            return true;
        });
    }

    /**
     * 执行集团
     *
     * @param filterGroupCodes 过滤集团编码, 为空时执行所有集团
     * @param callback         集团执行回调
     * @return 是否全部执行成功
     * @throws InterruptedException 线程中断
     * @since v6.0.0
     */
    protected boolean handlerGroup(
            @Nullable Set<String> filterGroupCodes,
            @NonNull FailCallbackFunction<GroupDTO> callback)
            throws InterruptedException {
        log.info("[{}] ------------------------- 任务执行开始 -------------------------", logTag());
        XxlJobHelper.log("[{}] ------------------------- 任务执行开始 -------------------------", logTag());
        StopWatch stopWatch = StopWatch.createStarted();

        boolean isAllFailed = true;
        try {
            ThreadExecutor.ifInterruptThrow();
            List<GroupDTO> groups = midPermissionClientService().listGroupOfValid(false, false);
            if (CollectionUtils.isNotEmpty(filterGroupCodes)) {
                groups.removeIf(item -> !filterGroupCodes.contains(item.getGroupCode()));
            }
            for (GroupDTO group : groups) {
                ThreadExecutor.ifInterruptThrow();
                if (BooleanUtils.isTrue(executeGroup(group, callback))) {
                    isAllFailed = false;
                }
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            LogExecutor.instance().error(log, logTag(), "集团任务执行异常", e);
        } finally {
            stopWatch.stop();
            log.info("[{}] ------------------------- 任务执行结束 [{}ms] -------------------------",
                    logTag(), stopWatch.getTime());
            XxlJobHelper.log("[{}] ------------------------- 任务执行结束 [{}ms] -------------------------",
                    logTag(), stopWatch.getTime());
        }
        if (isAllFailed) {
            XxlJobHelper.handleFail();
        }
        return isAllFailed;
    }

    /**
     * 根据上下文异步执行集团
     *
     * @param callback 集团执行回调
     * @throws InterruptedException 线程中断
     * @since 7.250801.0
     */
    protected void asyncHandlerGroupByContext(
            @NonNull FailCallbackConsumer<GroupDTO> callback)
            throws InterruptedException {
        asyncHandlerGroup(parseFilterGroupCodesFromContext(), callback);
    }

    /**
     * 异步执行集团
     *
     * @param filterGroupCodes 过滤集团编码, 为空时执行所有集团
     * @param callback         集团执行回调
     * @throws InterruptedException 线程中断
     * @since v6.0.0
     */
    protected void asyncHandlerGroup(
            @Nullable Set<String> filterGroupCodes,
            @NonNull FailCallbackConsumer<GroupDTO> callback)
            throws InterruptedException {
        log.info("[{}] ------------------------- 任务异步执行开始 -------------------------", logTag());
        XxlJobHelper.log("[{}] ------------------------- 任务异步执行开始 -------------------------", logTag());
        StopWatch stopWatch = StopWatch.createStarted();

        try {
            ThreadExecutor.ifInterruptThrow();
            List<GroupDTO> groups = midPermissionClientService().listGroupOfValid(false, false);
            if (CollectionUtils.isNotEmpty(filterGroupCodes)) {
                groups.removeIf(item -> !filterGroupCodes.contains(item.getGroupCode()));
            }
            AsyncTaskExecutor executor = BusinessThreadPoolExecutor.ttlExecutor();
            for (GroupDTO group : groups) {
                ThreadExecutor.ifInterruptThrow();
                executor.execute(() -> executeGroup(group, item -> {
                    callback.accept(item);
                    return true;
                }));
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            LogExecutor.instance().error(log, logTag(), "集团异步任务执行异常", e);
        } finally {
            stopWatch.stop();
            log.info("[{}] ------------------------- 任务异步执行结束 [{}ms] -------------------------",
                    logTag(), stopWatch.getTime());
            XxlJobHelper.log("[{}] ------------------------- 任务异步执行结束 [{}ms] -------------------------",
                    logTag(), stopWatch.getTime());
            MDC.clear();
        }
    }

    /**
     * 根据上下文执行项目
     *
     * @param callback 执行回调
     * @throws InterruptedException 线程中断
     * @since 7.250801.0
     */
    protected void handlerProjectByContext(
            @NonNull FailCallbackConsumer<ProjectDTO> callback)
            throws InterruptedException {
        handlerProject(parseFilterGroupCodesFromContext(), parseFilterProjectIdsFromContext(), callback);
    }

    /**
     * 执行项目
     *
     * @param filterGroupCodes 集团编码
     * @param filterProjectIds 项目ID
     * @param callback         执行回调
     * @throws InterruptedException 线程中断
     * @since v6.0.0
     */
    protected void handlerProject(
            @Nullable Set<String> filterGroupCodes,
            @Nullable Set<String> filterProjectIds,
            @NonNull FailCallbackConsumer<ProjectDTO> callback)
            throws InterruptedException {
        handlerProject(filterGroupCodes, filterProjectIds, project -> {
            callback.accept(project);
            return true;
        });
    }

    /**
     * 根据上下文执行项目
     *
     * @param callback 执行回调
     * @throws InterruptedException 线程中断
     * @since 7.250801.0
     */
    protected boolean handlerProjectByContext(
            @NonNull FailCallbackFunction<ProjectDTO> callback)
            throws InterruptedException {
        return handlerProject(parseFilterGroupCodesFromContext(), parseFilterProjectIdsFromContext(), callback);
    }

    /**
     * 执行项目
     *
     * @param filterGroupCodes 集团编码
     * @param filterProjectIds 项目ID
     * @param callback         执行回调
     * @return 是否全部执行成功
     * @throws InterruptedException 线程中断
     * @since v6.0.0
     */
    protected boolean handlerProject(
            @Nullable Set<String> filterGroupCodes,
            @Nullable Set<String> filterProjectIds,
            @NonNull FailCallbackFunction<ProjectDTO> callback)
            throws InterruptedException {
        return handlerGroup(filterGroupCodes, group -> {
            boolean isAllFailed = true;
            try {
                ThreadExecutor.ifInterruptThrow();
                List<ProjectDTO> projects = midPermissionClientService().listProjectOfValid(
                        false, false, group.getGroupCode(), filterProjectIds);
                for (ProjectDTO project : projects) {
                    ThreadExecutor.ifInterruptThrow();
                    if (BooleanUtils.isTrue(executeProject(group, project, callback))) {
                        isAllFailed = false;
                    }
                }
            } catch (InterruptedException e) {
                throw e;
            } catch (Exception e) {
                LogExecutor.instance().error(log, logTag(), "项目任务执行异常", e);
            }
            return !isAllFailed;
        });
    }

    /**
     * 根据上下文异步执行项目
     *
     * @param callback 执行回调
     * @throws InterruptedException 线程中断
     * @since 7.250801.0
     */
    protected void asyncHandlerProjectByContext(
            @NonNull FailCallbackConsumer<ProjectDTO> callback)
            throws InterruptedException {
        asyncHandlerProject(parseFilterGroupCodesFromContext(), parseFilterProjectIdsFromContext(), callback);
    }

    /**
     * 异步执行项目
     *
     * @param filterGroupCodes 集团编码
     * @param filterProjectIds 项目ID
     * @param callback         执行回调
     * @throws InterruptedException 线程中断
     * @since v6.0.0
     */
    protected void asyncHandlerProject(
            @Nullable Set<String> filterGroupCodes,
            @Nullable Set<String> filterProjectIds,
            @NonNull FailCallbackConsumer<ProjectDTO> callback)
            throws InterruptedException {
        asyncHandlerGroup(filterGroupCodes, group -> {
            try {
                ThreadExecutor.ifInterruptThrow();
                List<ProjectDTO> projects = midPermissionClientService().listProjectOfValid(
                        false, false, group.getGroupCode(), filterProjectIds);
                AsyncTaskExecutor executor = BusinessThreadPoolExecutor.ttlExecutor();
                for (ProjectDTO project : projects) {
                    ThreadExecutor.ifInterruptThrow();
                    executor.execute(() -> executeProject(group, project, item -> {
                        callback.accept(item);
                        return true;
                    }));
                }
            } catch (InterruptedException e) {
                throw e;
            } catch (Exception e) {
                LogExecutor.instance().error(log, logTag(), "项目任务执行异常", e);
            }
        });
    }

    /**
     * 解析执行参数
     *
     * @param cls          参数类
     * @param failCallback 解析失败回调
     * @param <T>          参数类型
     * @return 执行参数
     * @since v6.0.0
     */
    protected <T> T parseParam(@NonNull Class<T> cls, @Nullable Supplier<T> failCallback) {
        String jobParam = XxlJobHelper.getJobParam();
        if (StringUtils.isBlank(jobParam)) {
            return Objects.nonNull(failCallback) ? failCallback.get() : null;
        }
        try {
            return SpringUtil.getBean(ObjectMapper.class).readValue(jobParam, cls);
        } catch (Exception e) {
            LogExecutor.instance().error(log, logTag(), "解析执行参数失败", e,
                    Collections.singletonList(Pair.of("jobParam", jobParam)));
            return Objects.nonNull(failCallback) ? failCallback.get() : null;
        }
    }

    /**
     * 从上下文中解析过滤的项目ID
     *
     * @return 项目ID
     * @since 7.250801.0
     */
    private Set<String> parseFilterProjectIdsFromContext() {
        String projectId = ContextUtil.getProjectId();
        return StringUtils.isNotBlank(projectId) ? Sets.newHashSet(projectId) : null;
    }

    /**
     * 从上下文中解析过滤的集团编码
     *
     * @return 集团编码
     * @since 7.250801.0
     */
    private Set<String> parseFilterGroupCodesFromContext() {
        String groupCode = ContextUtil.getGroupCode();
        return StringUtils.isNotBlank(groupCode) ? Sets.newHashSet(groupCode) : null;
    }

    /**
     * 运维平台客户端服务
     *
     * @return 运维平台客户端服务
     * @since 7.250202.0
     */
    private MidPermissionClientService midPermissionClientService() {
        return SpringUtil.getBean(MidPermissionClientService.class);
    }

    /**
     * 执行集团
     *
     * @param group    集团
     * @param callback 回调方法
     * @return 是否执行成功
     * @since v6.0.0
     */
    private Boolean executeGroup(@NonNull GroupDTO group, @NonNull FailCallbackFunction<GroupDTO> callback) {
        ContextUtil.setGroupCode(group.getGroupCode());
        ContextUtil.setTenant(group.getGroupCode());
        MDC.put(ContextConstants.LOG_TRACE_ID, group.getGroupCode());
        log.info("[{}-{}] -------------------- 集团异步执行开始 --------------------",
                logTag(), group.getGroupCode());
        XxlJobHelper.log("[{}-{}] -------------------- 集团异步执行开始 --------------------",
                logTag(), group.getGroupCode());
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            return callback.apply(group);
        } catch (Exception e) {
            LogExecutor.group(group.getGroupCode()).error(log, logTag(), "集团异步执行异常", e);
        } finally {
            stopWatch.stop();
            log.info("[{}-{}] -------------------- 集团异步执行结束 [{}ms] --------------------",
                    logTag(), group.getGroupCode(), stopWatch.getTime());
            XxlJobHelper.log("[{}-{}] -------------------- 集团异步执行结束 [{}ms] --------------------",
                    logTag(), group.getGroupCode(), stopWatch.getTime());
            MDC.clear();
        }
        return false;
    }

    /**
     * 执行项目
     *
     * @param group    集团
     * @param project  项目
     * @param callback 回调方法
     * @return 是否执行成功
     * @since v6.0.0
     */
    private Boolean executeProject(
            @NonNull GroupDTO group,
            @NonNull ProjectDTO project,
            @NonNull FailCallbackFunction<ProjectDTO> callback) {
        ContextUtil.setProjectId(project.getProjectId());
        MDC.put(ContextConstants.LOG_TRACE_ID, group.getGroupCode() + "-" + project.getProjectId());
        log.info("[{}-{}-{}] --------------- [{}] 项目执行开始 ---------------",
                logTag(), group.getGroupCode(), project.getProjectId(), project.getProjectLocalName());
        XxlJobHelper.log("[{}-{}-{}] --------------- [{}] 项目执行开始 ---------------",
                logTag(), group.getGroupCode(), project.getProjectId(), project.getProjectLocalName());
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            return callback.apply(project);
        } catch (Exception e) {
            LogExecutor.project(project).error(log, logTag(), "项目执行异常", e);
        } finally {
            stopWatch.stop();
            log.info("[{}-{}-{}] --------------- [{}] 项目执行结束 [{}ms] ---------------",
                    logTag(), group.getGroupCode(), project.getProjectId(),
                    project.getProjectLocalName(), stopWatch.getTime());
            XxlJobHelper.log("[{}-{}-{}] --------------- [{}] 项目执行结束 [{}ms] ---------------",
                    logTag(), group.getGroupCode(), project.getProjectId(),
                    project.getProjectLocalName(), stopWatch.getTime());
            MDC.clear();
        }
        return false;
    }

    /**
     * 失败回调函数式接口
     *
     * @param <T> 参数类型
     * @author JiYinchuan
     * @since v6.0.0
     */
    @FunctionalInterface
    protected interface FailCallbackFunction<T> {

        /**
         * Applies this function to the given argument.
         *
         * @param t the function argument
         * @return the function result
         * @throws InterruptedException 线程中断
         */
        Boolean apply(T t) throws InterruptedException;

    }

    /**
     * 失败回调函数式接口
     *
     * @param <T> 参数类型
     * @author JiYinchuan
     * @since v6.0.0
     */
    @FunctionalInterface
    protected interface FailCallbackConsumer<T> {

        /**
         * Performs this operation on the given argument.
         *
         * @param t the input argument
         * @throws InterruptedException 线程中断
         */
        void accept(T t) throws InterruptedException;

    }
}
