package com.persagy.apccontrol.task;

import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.framework.common.context.ContextUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 基础边缘端任务
 *
 * @author JiYinchuan
 * @since 7.250602.0
 */
public abstract class BaseEdgeTask implements LogComponent {

    @Autowired
    private ProjectCfg projectCfg;

    /**
     * 初始化
     *
     * @since 7.250602.0
     */
    public void initGroupCodeAndProjectId() {
        ContextUtil.setGroupCode(getGroupCode());
        ContextUtil.setTenant(getGroupCode());
        ContextUtil.setProjectId(getProjectId());
    }

    /**
     * 获取集团编码
     *
     * @return 集团编码
     * @since 7.250602.0
     */
    public String getGroupCode() {
        return projectCfg.getGroupCode();
    }

    /**
     * 获取项目ID
     *
     * @return 项目ID
     * @since 7.250602.0
     */
    public String getProjectId() {
        return projectCfg.getProjectId();
    }
}
