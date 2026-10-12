package com.persagy.apccontrol.basic.executor.log;

import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import org.slf4j.Logger;
import org.springframework.lang.NonNull;

/**
 * 日志组件项目执行器
 *
 * @author JiYinchuan
 * @since 5.240501.0
 */
public class LogComponentProjectExecutor extends LogProjectExecutor implements LogComponentProvider {

    /**
     * 日志组件
     */
    protected final LogComponent component;

    /**
     * 构造器
     *
     * @param component  日志组件
     * @param projectDTO 项目DTO
     * @since 5.240501.0
     */
    public LogComponentProjectExecutor(@NonNull LogComponent component, @NonNull ProjectDTO projectDTO) {
        super(projectDTO);
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
