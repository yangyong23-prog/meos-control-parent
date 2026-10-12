package com.persagy.apccontrol.basic.model.vo.security;

import lombok.Data;

/**
 * 安防设备实例数量统计(安防功能最低要求)
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/12/04 19:34
 * @since v5.241201.0
 */
@Data
public class SecurityFunctionVO {

    /**
     * 通行状态功能
     */
    private Boolean accessDemandFunction;

    /**
     * 撤布防状态功能
     */
    private Boolean deploymentStateFunction;

    /**
     * 空间安防整体功能
     */
    private Boolean overallFunction;

}
