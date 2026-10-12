package com.persagy.apccontrol.basic.utils.context;

import com.persagy.apccontrol.basic.model.message.dtx.DtxPublishMessageEvent;
import com.persagy.framework.common.context.ContextUtil;
import org.jspecify.annotations.NonNull;

/**
 * 上下文工具类
 *
 * @author JiYinchuan
 * @since 7.250602.0
 */
public class ContextUtils {

    /**
     * 初始化集团编码和项目ID
     *
     * @param message 消息
     * @since 7.250602.0
     */
    public static void initGroupCodeProjectId(DtxPublishMessageEvent.@NonNull Message message) {
        initGroupCodeProjectId(message.getGroupCode(), message.getProjectId());
    }

    /**
     * 初始化集团编码和项目ID
     *
     * @param projectId 项目ID
     * @since 7.250602.0
     */
    public static void initGroupCodeProjectId(@NonNull String projectId) {
        ContextUtil.setProjectId(projectId);
    }

    /**
     * 初始化集团编码和项目ID
     *
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @since 7.250602.0
     */
    public static void initGroupCodeProjectId(@NonNull String groupCode, @NonNull String projectId) {
        ContextUtil.setGroupCode(groupCode);
        ContextUtil.setTenant(groupCode);
        ContextUtil.setProjectId(projectId);
    }
}
