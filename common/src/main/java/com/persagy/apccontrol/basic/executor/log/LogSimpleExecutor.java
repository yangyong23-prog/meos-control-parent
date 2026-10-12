package com.persagy.apccontrol.basic.executor.log;

import org.springframework.lang.Nullable;

/**
 * 简单日志执行器
 *
 * @author JiYinchuan
 * @since 5.240501.0
 */
public class LogSimpleExecutor extends BaseLogExecutor implements LogProvider {

    /**
     * 构造器
     *
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @since 5.240501.0
     */
    public LogSimpleExecutor(@Nullable String groupCode, @Nullable String projectId) {
        super(groupCode, projectId);
    }
}
