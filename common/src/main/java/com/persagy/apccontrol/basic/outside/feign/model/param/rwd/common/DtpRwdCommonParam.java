package com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common;

import com.persagy.framework.common.context.ContextUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

/**
 * 物理世界公共请求参数
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@NoArgsConstructor
public class DtpRwdCommonParam {

    /**
     * 用户ID
     */
    protected String userId = "systemId";

    /**
     * 集团编码
     */
    protected String groupCode;

    /**
     * 项目ID
     */
    protected String projectId;

    /**
     * 构造器
     *
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @since v5.6.0
     */
    protected DtpRwdCommonParam(String groupCode, String projectId) {
        this.groupCode = groupCode;
        this.projectId = projectId;
    }

    /**
     * 创建当前实例
     *
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @return 当前实例
     * @since v5.6.0
     */
    public static DtpRwdCommonParam instance(@NonNull String groupCode, @NonNull String projectId) {
        return new DtpRwdCommonParam(groupCode, projectId);
    }

    /**
     * 创建当前实例
     *
     * @param projectId 项目ID
     * @return 当前实例
     * @since v5.6.0
     */
    public static DtpRwdCommonParam instance(@NonNull String projectId) {
        return new DtpRwdCommonParam(ContextUtil.getGroupCode(), projectId);
    }

    /**
     * 创建当前实例
     *
     * @return 当前实例
     * @since 7.250102.0
     */
    public static DtpRwdCommonParam instance() {
        return new DtpRwdCommonParam(ContextUtil.getGroupCode(), ContextUtil.getProjectId());
    }

}
