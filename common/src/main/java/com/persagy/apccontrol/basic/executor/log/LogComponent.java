package com.persagy.apccontrol.basic.executor.log;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.persagy.apccontrol.basic.config.cache.LocalContextStorage;
import com.persagy.apccontrol.basic.structure.performance.StopWatchContext;
import com.persagy.framework.common.context.ContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.lang.NonNull;

/**
 * 日志组件
 *
 * @author JiYinchuan
 * @since 5.240501.0
 */
public interface LogComponent {

    /**
     * 获取日志对象
     *
     * @return 日志对象
     * @since 5.240501.0
     */
    @JsonIgnore
    default Logger log() {
        return LoggerFactory.getLogger(this.getClass());
    }

    /**
     * 是否启用TRACE模式
     *
     * @return 是否启用TRACE模式
     * @since 5.241201.0
     */
    @JsonIgnore
    default boolean isTraceEnabled() {
        return log().isTraceEnabled() || LocalContextStorage.isRequestDebug(Level.TRACE);
    }

    /**
     * 是否启用DEBUG模式
     *
     * @return 是否启用DEBUG模式
     * @since 5.241001.0
     */
    @JsonIgnore
    default boolean isDebugEnabled() {
        return log().isDebugEnabled() || LocalContextStorage.isRequestDebug(Level.DEBUG);
    }

    /**
     * 是否启用WARN模式
     *
     * @return 是否启用WARN模式
     * @since 5.241201.0
     */
    @JsonIgnore
    default boolean isWarnEnabled() {
        return log().isWarnEnabled() || LocalContextStorage.isRequestDebug(Level.WARN);
    }

    /**
     * 是否启用ERROR模式
     *
     * @return 是否启用ERROR模式
     * @since 5.241201.0
     */
    @JsonIgnore
    default boolean isErrorEnabled() {
        return log().isErrorEnabled() || LocalContextStorage.isRequestDebug(Level.ERROR);
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @JsonIgnore
    String logTag();

    /**
     * 日志实例
     *
     * @return 日志实例
     * @since 5.240501.0
     */
    @JsonIgnore
    default LogComponentProvider logInstance() {
        return LogExecutor.instance(this);
    }

    /**
     * 项目日志实例
     *
     * @return 项目日志实例
     * @since 5.240501.0
     */
    @JsonIgnore
    default LogComponentProvider logProjectInstance() {
        return LogExecutor.project(this, ContextUtil.getProjectId());
    }

    /**
     * 项目日志实例
     *
     * @param projectId 项目ID
     * @return 项目日志实例
     * @since 5.240501.0
     */
    @JsonIgnore
    default LogComponentProvider logProjectInstance(@NonNull String projectId) {
        return LogExecutor.project(this, projectId);
    }

    /**
     * 获取StopWatch
     *
     * @return StopWatch
     * @since 7.250302.0
     */
    @JsonIgnore
    default StopWatchContext globalStopWatch() {
        return LocalContextStorage.stopWatch(logTag());
    }
}
