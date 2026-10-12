package com.persagy.apccontrol.basic.executor.log;

import org.slf4j.Logger;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * 日志组件执行器
 *
 * @author JiYinchuan
 * @since 5.240501.0
 */
public class LogComponentSimpleExecutor extends LogSimpleExecutor implements LogComponentProvider {

    /**
     * 日志组件
     */
    protected LogComponent component;

    /**
     * 构造器
     *
     * @param component 日志组件
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @since 5.240501.0
     */
    public LogComponentSimpleExecutor(
            @NonNull LogComponent component,
            @Nullable String groupCode, @Nullable String projectId) {
        super(groupCode, projectId);
        this.component = component;
    }

    @Override
    public Logger log() {
        return component.log();
    }

    @Override
    public String logTag() {
        return component.logTag();
    }
}
