package com.persagy.apccontrol.basic.model.message;

import com.persagy.framework.common.context.ContextUtil;
import lombok.Data;

/**
 * 基础消息
 *
 * @author JiYinchuan
 * @since v5.8.0
 */
@Data
public abstract class BaseMessage {

    /**
     * 集团编码
     */
    private String groupCode;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 构造器
     *
     * @since v5.8.0
     */
    public BaseMessage() {
        this.groupCode = ContextUtil.getGroupCode();
        this.projectId = ContextUtil.getProjectId();
        this.userId = ContextUtil.getUserId();
        this.userName = ContextUtil.getUserName();
    }
}
