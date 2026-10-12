package com.persagy.apccontrol.controller.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ：wzq
 * @description：
 * @date ：Created in 2023/10/10 2:50 PM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectInfoVO {
    /**
     * 集团编码
     */
    private String groupCode;
    /**
     * 项目id
     */
    private String projectId;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * meoi跳转地址前缀
     */
    private String meoiUrl;

    /**
     * meoi跳转平台名称
     */
    private String systemAppName;
}
